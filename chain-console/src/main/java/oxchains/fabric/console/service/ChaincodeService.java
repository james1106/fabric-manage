package oxchains.fabric.console.service;

import org.apache.commons.io.FileUtils;
import org.hyperledger.fabric.sdk.ChainCodeID;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import oxchains.fabric.console.data.ChaincodeRepo;
import oxchains.fabric.console.domain.ChainCodeInfo;
import oxchains.fabric.console.rest.common.QueryResult;
import oxchains.fabric.console.rest.common.TxResult;
import oxchains.fabric.sdk.FabricSDK;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.hyperledger.fabric.sdk.ChainCodeResponse.Status.SUCCESS;

/**
 * @author aiet
 */
@Service
public class ChaincodeService {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    private FabricSDK fabricSDK;
    private ChaincodeRepo chaincodeRepo;

    public ChaincodeService(@Autowired FabricSDK fabricSDK, @Autowired ChaincodeRepo chaincodeRepo) {
        this.fabricSDK = fabricSDK;
        this.chaincodeRepo = chaincodeRepo;
    }

    @Value("${fabric.chaincode.path}") private String path;
    @Value("${fabric.tx.timeout}") private int txTimeout;

    public boolean cacheChaincode(String name, String version, String lang, MultipartFile file) {
        try {
            File dir = new File(String.format("%s/src/%s", path, name));
            if (!dir.exists()) FileUtils.forceMkdir(dir);
            File target = new File(String.format("%s/src/%s/%s-%s-%s.go", path, name, name, version, now().format(ISO_LOCAL_DATE_TIME)));
            file.transferTo(target);
            chaincodeRepo.save(new ChainCodeInfo(name, version, lang, target.getPath()));
            return true;
        } catch (Exception e) {
            LOG.error("failed to cache chaincode {}-{}", name, version, e);
        }
        return false;
    }

    public List<TxResult> installCCOnPeer(String name, String version, String lang, String[] peers) {
        ChainCodeID chaincode = ChainCodeID
          .newBuilder()
          .setName(name)
          .setVersion(version)
          .setPath(name)
          .build();

        List<Peer> peerList = Arrays
          .stream(peers)
          .map(p -> fabricSDK
            .getPeer(p)
            .orElse(null))
          .filter(Objects::nonNull)
          .collect(toList());
        if (peerList.size() == peers.length) {
            List<TxResult> list = fabricSDK
              .installChaincodeOnPeer(chaincode, path, lang, peerList)
              .stream()
              .map(RESPONSE2TXRESULT_FUNC)
              .collect(toList());

            try {
                ChainCodeInfo chainCodeInfo = chaincodeRepo.findByNameAndVersion(name, version);
                chainCodeInfo.setInstalled(list.isEmpty() ? 0 : 1);
                chaincodeRepo.save(chainCodeInfo);
            } catch (Exception e) {
                LOG.error("failed to update install status of chaincode {}-{}", name, version, e);
            }

            return list;
        }
        return emptyList();
    }

    public Optional<TxResult> instantiate(String name, String version, String[] params, MultipartFile endorsement) {
        ChainCodeID chaincode = ChainCodeID
          .newBuilder()
          .setName(name)
          .setVersion(version)
          .setPath(name)
          .build();
        try {
            File dir = new File(String.format("%s/endorsement/%s", path, name));
            if (!dir.exists()) FileUtils.forceMkdir(dir);
            File endorsementYaml = new File(String.format("%s/endorsement/%s/%s-%s-(%s).yaml", path, name, name, version, now().toString()));
            endorsement.transferTo(endorsementYaml);

            ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
            chaincodeEndorsementPolicy.fromYamlFile(endorsementYaml);

            return Optional.of(fabricSDK
              .instantiateChaincode(chaincode, chaincodeEndorsementPolicy, params)
              .thenApplyAsync(RESPONSE2TXRESULT_FUNC)
              .get(txTimeout, SECONDS));
        } catch (Exception e) {
            LOG.error("failed to instantiate chaincode {}-{} with {}", name, version, params, e);
        }
        return empty();
    }

    public List<ChainCodeInfo> chaincodes() {
        try {
            return newArrayList(chaincodeRepo.findAll());
        } catch (Exception e) {
            LOG.error("failed to fetch chaincodes");
        }
        return emptyList();
    }

    public Optional<TxResult> invoke(String name, String version, String... params) {
        ChainCodeID chaincode = ChainCodeID
          .newBuilder()
          .setName(name)
          .setVersion(version)
          .setPath(name)
          .build();
        try {
            return Optional.of(fabricSDK
              .invokeChaincode(chaincode, params)
              .thenApplyAsync(RESPONSE2TXRESULT_FUNC)
              .get(txTimeout, SECONDS));
        } catch (Exception e) {
            LOG.error("failed to invoke chaincode {}-{} with {}", name, version, params, e);
        }
        return empty();
    }

    private static Function<ProposalResponse, TxResult> RESPONSE2TXRESULT_FUNC = proposalResponse -> nonNull(proposalResponse) ? new TxResult<>(proposalResponse.getTransactionID(), proposalResponse
      .getPeer()
      .getName(), proposalResponse.getStatus() == SUCCESS ? 1 : 0) : null;

    public Optional<QueryResult> query(String name, String version, String[] args) {
        ChainCodeID chaincode = ChainCodeID
          .newBuilder()
          .setName(name)
          .setVersion(version)
          .setPath(name)
          .build();
        try {
            return Optional
              .of(fabricSDK.queryChaincode(chaincode, args))
              .map(QueryResult::new);
        } catch (Exception e) {
            LOG.error("failed to invoke chaincode {}-{} with {}", name, version, args, e);
        }
        return empty();
    }

}
