package oxchains.fabric.console.service;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.io.FileUtils;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import oxchains.fabric.console.auth.JwtAuthentication;
import oxchains.fabric.console.data.ChaincodeRepo;
import oxchains.fabric.console.domain.ChainCodeInfo;
import oxchains.fabric.console.domain.User;
import oxchains.fabric.console.rest.common.QueryResult;
import oxchains.fabric.console.rest.common.TxPeerResult;
import oxchains.fabric.console.rest.common.TxResult;
import oxchains.fabric.sdk.FabricSDK;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.hyperledger.fabric.sdk.ChaincodeResponse.Status.SUCCESS;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import static oxchains.fabric.sdk.domain.CAUser.fromUser2;

/**
 * @author aiet
 */
@Service(version="1.0.0")
public class ChaincodeServiceImpl implements ChaincodeService{

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    private FabricSDK fabricSDK;
    private ChaincodeRepo chaincodeRepo;

    public ChaincodeServiceImpl(@Autowired FabricSDK fabricSDK, @Autowired ChaincodeRepo chaincodeRepo) {
        this.fabricSDK = fabricSDK;
        this.chaincodeRepo = chaincodeRepo;
    }

    @Value("${fabric.chaincode.path}") private String path;
    @Value("${fabric.tx.timeout}") private int txTimeout;

    public boolean cacheChaincode(String name, String version, String lang, MultipartFile file) {
        try {
            File chaincodePath = new File(String.format("%s/src/%s-%s", path, name, version));
            if (chaincodePath.exists()) {
                LOG.warn("chaincode {}-{} already exists", name, version);
                return false;
            } else FileUtils.forceMkdir(chaincodePath);

            File target = new File(chaincodePath.getPath() + String.format("/%s-%s-%s.go", name, version, now().format(ISO_LOCAL_DATE_TIME)));
            if (target.exists()) {
                LOG.warn("target cache file {} already exists", target.getPath());
                return false;
            }
            file.transferTo(target);
            return userContext()
              .map(context -> {
                  ChainCodeInfo chainCodeInfo = new ChainCodeInfo(name, version, lang, target
                    .getPath()
                    .replace(path, ""));
                  chainCodeInfo.setAffiliation(context.getAffiliation());
                  chaincodeRepo.save(chainCodeInfo);
                  LOG.info("chaincode {}-{} written in {} cached to file {}", name, version, lang, target.getPath());
                  return true;
              })
              .orElse(false);
        } catch (Exception e) {
            LOG.error("failed to cache chaincode {}-{}", name, version, e);
        }
        return false;
    }

    private String chaincodePath(String name, String version) {
        return name + "-" + version;
    }

    public List<TxResult> installCCOnPeer(String chain, String name, String version, String lang, String... peers) {
        ChaincodeID chaincode = ChaincodeID
          .newBuilder()
          .setName(name)
          .setVersion(version)
          .setPath(chaincodePath(name, version))
          .build();

        if (noPeersYet(chain)) return emptyList();

        try {
            Optional<User> contextOptional = userContext();
            if (contextOptional.isPresent()) {
                User context = contextOptional.get();
                Optional<ChainCodeInfo> chainCodeInfoOptional = chaincodeRepo.findByNameAndVersionAndAffiliation(name, version, context.getAffiliation());
                if (!chainCodeInfoOptional.isPresent()) return emptyList();
                ChainCodeInfo chainCodeInfo = chainCodeInfoOptional.get();
                Set<String> installedPeers = chainCodeInfo.getInstalled();
                List<String> peers2Install = Arrays.asList(peers);
                installedPeers.retainAll(peers2Install);
                List<Peer> peerList = peers2Install
                  .stream()
                  .map(p -> fabricSDK
                    .getPeer(p, chain)
                    .orElse(null))
                  .filter(Objects::nonNull)
                  .filter(p -> !installedPeers.contains(p.getName()))
                  .collect(toList());

                List<TxResult> list = installedPeers
                  .stream()
                  .map(installedPeer -> new TxResult<>(null, installedPeer, 1))
                  .collect(toList());

                if (!peerList.isEmpty()) {
                    fabricSDK
                      .withUserContext(fromUser2(context))
                      .installChaincodeOnPeer(chaincode, chain, path, lang, peerList)
                      .stream()
                      .map(resp -> {
                          if (resp.getStatus() == SUCCESS) {
                              chainCodeInfo.addInstalled(resp
                                .getPeer()
                                .getName());
                              LOG.info("chaincode {}-{} installed on peer", name, version, resp
                                .getPeer()
                                .getName());
                          }
                          return resp;
                      })
                      .map(RESPONSE2TXRESULT_FUNC)
                      .forEach(list::add);
                    chaincodeRepo.save(chainCodeInfo);
                }
                return list;

            }

        } catch (Exception e) {
            LOG.error("failed to update install status of chaincode {}-{}", name, version, e);
        }
        return emptyList();
    }

    public Optional<TxPeerResult> instantiate(String chain, String name, String version, MultipartFile endorsement, String... params) {
        ChaincodeID chaincode = ChaincodeID
          .newBuilder()
          .setName(name)
          .setVersion(version)
          .setPath(chaincodePath(name, version))
          .build();

        if (noPeersYet(chain)) return empty();

        try {
            Optional<User> contextOptional = userContext();
            if (contextOptional.isPresent()) {
                User context = contextOptional.get();
                Optional<ChainCodeInfo> chainCodeInfoOptional = chaincodeRepo.findByNameAndVersionAndAffiliation(name, version, context.getAffiliation());
                if (!chainCodeInfoOptional.isPresent()) return empty();
                ChainCodeInfo chainCodeInfo = chainCodeInfoOptional.get();
                File dir = new File(String.format("%s/endorsement/%s", path, name));
                if (!dir.exists()) FileUtils.forceMkdir(dir);
                File endorsementYaml = new File(String.format("%s/endorsement/%s/%s-%s-(%s).yaml", path, name, name, version, now().toString()));
                endorsement.transferTo(endorsementYaml);

                ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
                chaincodeEndorsementPolicy.fromYamlFile(endorsementYaml);

                LOG.info("instantiating chaincode {}-{} with endorsement {}", name, version, endorsementYaml.getPath());

                return Optional.ofNullable(fabricSDK
                  .withUserContext(fromUser2(context))
                  .instantiateChaincode(chaincode, chain, chaincodeEndorsementPolicy, params)
                  .thenApplyAsync(RESPONSE2TXPEERRESULT_FUNC)
                  .thenApplyAsync(txPeerResult -> {
                      if (txPeerResult.getSuccess() == 1) {
                          chainCodeInfo.addInstantiated(txPeerResult.getPeer());
                          chaincodeRepo.save(chainCodeInfo);
                          LOG.info("chaincode {}-{} instantiated on peer {}", name, version, txPeerResult.getPeer());
                      }
                      return txPeerResult;
                  })
                  .get(txTimeout, SECONDS));
            }
        } catch (Exception e) {
            LOG.error("failed to instantiate chaincode {}-{} with {}", name, version, params, e);
        }
        return empty();
    }

    public List<ChainCodeInfo> chaincodes() {
        try {
            return userContext()
              .map(context -> newArrayList(chaincodeRepo.findByAffiliation(context.getAffiliation())))
              .orElse(newArrayList());
        } catch (Exception e) {
            LOG.error("failed to fetch chaincodes", e);
        }
        return emptyList();
    }

    private Optional<User> userContext() {
        return ((JwtAuthentication) getContext().getAuthentication()).user();
    }

    public Optional<TxPeerResult> invoke(String chain, String name, String version, String... params) {
        ChaincodeID chaincode = ChaincodeID
          .newBuilder()
          .setName(name)
          .setVersion(version)
          .setPath(chaincodePath(name, version))
          .build();

        if (noPeersYet(chain)) return empty();

        String[] newArgs = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            try {
                newArgs[i] = URLDecoder.decode(params[i], "utf-8");
            } catch (UnsupportedEncodingException ignored) {
                newArgs[i] = params[i];
            }
        }

        LOG.info("invoking chaincode {}-{} with: {}", name, version, newArgs);
        return userContext().map(context -> {
            try {
                return fabricSDK
                  .withUserContext(fromUser2(context))
                  .invokeChaincode(chain, chaincode, newArgs)
                  .thenApplyAsync(RESPONSE2TXPEERRESULT_FUNC)
                  .get(txTimeout, SECONDS);
            } catch (Exception e) {
                LOG.error("failed to invoke chaincode {}-{} with {}", name, version, newArgs, e);
            }
            return null;
        });
    }

    private static Function<ProposalResponse, TxPeerResult> RESPONSE2TXPEERRESULT_FUNC = proposalResponse -> nonNull(proposalResponse) ? new TxPeerResult(proposalResponse.getTransactionID(), proposalResponse
      .getPeer()
      .getName(), proposalResponse.getStatus() == SUCCESS ? 1 : 0) : null;

    private static Function<ProposalResponse, TxResult> RESPONSE2TXRESULT_FUNC = proposalResponse -> nonNull(proposalResponse) ? new TxResult<>(proposalResponse.getTransactionID(), proposalResponse
      .getPeer()
      .getName(), proposalResponse.getStatus() == SUCCESS ? 1 : 0) : null;

    public Optional<QueryResult> query(String chain, String name, String version, String... args) {
        ChaincodeID chaincode = ChaincodeID
          .newBuilder()
          .setName(name)
          .setVersion(version)
          .setPath(name)
          .build();

        if (noPeersYet(chain)) return empty();

        LOG.info("querying chaincode {}-{} with {}", name, version, args);
        return userContext()
          .map(context -> fabricSDK
            .withUserContext(fromUser2(context))
            .queryChaincode(chain, chaincode, args))
          .map(QueryResult::new);
    }

    private boolean noPeersYet(String chain) {
        return userContext()
          .map(u -> fabricSDK
            .withUserContext(fromUser2(u))
            .chainPeers(chain))
          .orElse(emptyList())
          .isEmpty();
    }

}
