# fabric-manage
hyperledger fabric chain management web console

## [chain-console](./chain-console)

backend with RESTful API for:
  - peer node: start, stop, status(network, operation, etc.)
  - chain(channel): create, join, blockinfo
  - ca user: login(enroll), register, revoke
  - chaincode: upload, delete, install(deploy), instantiate, invoke and query
  

## [FrontEnd](./FrontEnd)

### Getting Started

Checkout this repo, install dependencies, then start the app with the following:

```
> git clone https://git@github.com:zkjs/oxchain-invoice.git
> cd invoice-app/FrontEnd
> npm install
> npm start
```

##### 配置  

```
将 ./src/actions/types.js 中 ROOT_URL 修改为API服务器地址
```

##### 截屏

###### 节点
 ![image](./FrontEnd/screenshot/peer.png)  
 ![image](./screenshot/peer_detail.png)   
 ![image](./FrontEnd/screenshot/peer_add.png)  
 
###### 区块链  
 ![image](./FrontEnd/screenshot/chain.png)  
 ![image](./FrontEnd/screenshot/block.png)    
 
###### 用户
 ![image](./FrontEnd/screenshot/user.png)  
 ![image](./FrontEnd/screenshot/user_add.png)  
