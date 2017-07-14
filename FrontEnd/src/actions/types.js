export const ROOT_URL = 'http://localhost:9494';


export const AUTH_USER = 'auth_user';                               //登录
export const UNAUTH_USER = 'unauth_user';                           //退出登录
export const AUTH_ERROR = 'auth_error';                             //登录失败

export const REQUEST_SUCCESS = 'request_success';                   //http请求正确
export const REQUEST_ERROR = 'request_error';                       //http请求返回错误

export const FETCH_PEER_LIST = 'fetch_peer_list';                   //获取节点列表
export const FETCH_PEER_STATUS = 'fetch_peer_status';               //获取节点状态
export const FETCH_EVENTHUB_LIST = 'fetch_eventhub_list';           //获取EventHub列表
export const ADD_PEER_SUCCESS = 'add_peer_success';                 //添加节点成功
export const ADD_PEER_ERROR = 'add_peer_error';                     //添加节点失败
export const ENROLL_PEER_SUCCESS = 'enroll_peer_success';           //登录节点成功

export const FETCH_USER_LIST = 'fetch_user_list';                   //获取用户列表
export const ADD_USER_SUCCESS = 'add_user_success';                 //注册用户成功
export const ADD_USER_ERROR = 'add_user_error';                     //注册用户失败

export const FETCH_CHAIN_LIST = 'fetch_chain_list';                 //获取链列表信息
export const ADD_CHAIN = 'add_chain';                               //添加链
export const FETCH_CHAIN_INFO = 'fetch_chain_info';                 //获取Chain信息
export const FETCH_BLOCK_LIST = 'fetch_block_list';                 //获取区块列表
export const FETCH_BLOCK_INFO = 'fetch_block_info';                 //获取区块信息
export const FETCH_TRANSACTION_LIST = 'fetch_transaction_list';     //获取交易列表
export const FETCH_TRANSACTION_INFO = 'fetch_transaction_info';     //获取交易信息

export const FETCH_CHAINCODE_LIST = 'fetch_chain_code_list';        //获取合约列表
export const UPLOAD_CHAINCODE = 'upload_chain_code';                //上传合约
export const INSTALL_CHAINCODE = 'install_chain_code';              //部署合约
export const INIT_CHAINCODE = 'init_chain_code';                    //初始化合约
export const EXECUTE_CHAINCODE = 'execute_chain_code';              //执行合约
export const FETCH_CHAINCODE_INFO = 'fetch_chain_code_info';        //查询合约

export const UPDATE_CHAINCODE = 'update_chain_code';                //更新合约



export function getAuthorizedHeader() {
  return { authorization: 'Bearer '+localStorage.getItem('token') }
}
