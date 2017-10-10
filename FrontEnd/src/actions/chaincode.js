/**
 * oxchain ivoice app
 *
 *
 * Author: Jun
 * Email: iyakexi@gmail.com
 * Date: 05/05/2017
 *
 */

import axios from 'axios';
import {
  ROOT_URL,
  REQUEST_SUCCESS,
  REQUEST_ERROR,
  FETCH_CHAINCODE_LIST,
  UPLOAD_CHAINCODE,
  INSTALL_CHAINCODE,
  INIT_CHAINCODE,
    UPDATE_CHAINCODE,
  EXECUTE_CHAINCODE,
  FETCH_CHAINCODE_INFO,
  getAuthorizedHeader
} from './types';
import _ from 'lodash';

export function requestError(error) {
  return {
    type: REQUEST_ERROR,
    payload: error
  };
}

//合约列表
export function fetchChainCodeList() {
  return function(dispatch) {
    axios.get(`${ROOT_URL}/chaincode`, { headers: getAuthorizedHeader() })
      .then(response => dispatch({ type: FETCH_CHAINCODE_LIST, payload:response }))
      .catch( err => dispatch(requestError(err.message)) );
  }
}


/**
 * 上传合约
 * POST : multipart/form-data
 * @param name
 * @param version
 * @param lang
 * @param chaincode   : 文件
 * @param callback(err)   :  callback when http request response. if(success) err=null ; else err=[error message].
 */
export function uploadChainCode({ name, version, lang, chaincode }, callback) {
  console.log(`uploadChainCode: ${name}, ${version}, ${lang}, ${chaincode}`);
  console.log(chaincode)

  return function(dispatch) {
    let formData = new FormData();
    formData.append('name', name)
    formData.append('version', version)
    formData.append('lang', lang)
    formData.append('chaincode', chaincode[0])

     // let formData ={
     //     name:this.refs['name'].getDOMNode().value,
     //     version:this.refs['version'].getDOMNode().value,
     //     lang:this.refs['lang'].getDOMNode().value,
     //     chaincode:this.refs['chaincode[0]'].getDOMNode().value,
     //
     // }
    const headers = getAuthorizedHeader();
    axios.post(`${ROOT_URL}/chaincode/file`, formData, {headers: { 'content-type': 'multipart/form-data', ...headers }})
      .then(function(response) {
        if(response.data.status == 1) {// success
          dispatch({ type: UPLOAD_CHAINCODE, payload:response });
          callback();
        } else {// fail
          callback(response.data.message);
        }
      })
      .catch(function(err) {
        dispatch(requestError(err.message));
        callback(err.message);
      });
  }
}


/**
 * 部署合约
 * @param chain           :  链名称
 * @param name            :  合约名称
 * @param version
 * @param lang
 * @param peers           :  部署节点, 多个节点逗号分隔
 * @param callback(err)   :  callback when http request response. if(success) err=null ; else err=[error message].
 */
export function installChainCode({ chain, name, version, lang, peers }, callback) {
  console.log(`installChainCode: ${chain}, ${name}, ${version}, ${lang}, ${peers}`);

  return function(dispatch) {
      let formData = {
          "chain":chain,
          "name": name,
          "version": version,
          "lang": lang,
          "peers":peers
      }
      console.log("---部署合约---",formData)
      const headers = getAuthorizedHeader();
      axios.post(`${ROOT_URL}/chaincode/install`, formData, {headers: { 'content-type': 'application/json', ...headers }})
      .then(response => {

        if(response.data.status == 1) {// success
          const data = response.data.data;
          var err_msg = [];
          for(var i=0; i<peers.length; i++) {
            var peer = peers[i];
            for(var j=0; j<data.length; j++) {
              if(data[j][peer]===0) {
                err_msg.push(peer);
              }
            }
          }
          if(err_msg.length>0) {
            callback('以下节点部署失败: '+err_msg.join());
          } else {
            dispatch({ type: INSTALL_CHAINCODE, payload:response });
            callback();
          }
        } else {// fail
          callback(response.data.message);
        }

      })
      .catch( err => {
        dispatch(requestError(err.message));
        callback(err.message);
      } );
  }
}



/**
 * 初始化合约
 * POST : multipart/form-data
 * @param name
 * @param version
 * @param args
 * @param endorsement  : yaml file
 * @param callback(err)   :  callback when http request response. if(success) err=null ; else err=[error message].
 */
export function initChainCode({ chain, name, version, args, endorsement }, callback) {
  console.log(`initChainCode: ${chain}, ${name}, ${version}, ${args}, ${endorsement}`);

  return function(dispatch) {
      let formData = new FormData();
     formData.append('chain', chain)
     formData.append('name', name)
     formData.append('version', version)
     formData.append('args', args)
     formData.append('endorsement', endorsement[0])

       //  formData = {
       //     "chain":chain,
       //     "name": name,
       //     "version": version,
       //     "args": args,
       //     "endorsement":endorsement[0],
       // }
 console.log("---初始化合约+---",formData)

    const headers = getAuthorizedHeader();                                     //'application/json'
    axios.post(`${ROOT_URL}/chaincode`,formData , {headers: { 'content-type': 'multipart/form-data', ...headers }})
      .then(function(response) {

        if(response.data.status == 1) {// success

          dispatch({ type: INIT_CHAINCODE, payload:response });
          callback();
        } else {// fail
          callback(response.data.message);
        }
      })
      .catch(function(err) {
        dispatch(requestError(err.message));
        callback(err.message);
      });
  }
}

/**
 * 更新合约
 * POST : application/x-www-form-urlencoded
 * @param name
 * @param version
 * @param args
 * @param endorsement  : yaml file
 * @param callback(err)   :  callback when http request response. if(success) err=null ; else err=[error message].
 */
export function UpdateChainCode({ chain, name, version, args, endorsement }, callback) {
    console.log(`UpdateChainCode: ${chain}, ${name}, ${version}, ${args}, ${endorsement}`);

    return function(dispatch) {
        let formData = new FormData();
        formData.append('chain', chain)
        formData.append('name', name)
        formData.append('version', version)
        formData.append('args', args)
        formData.append('endorsement', endorsement[0])

        // formData = {
        //     "chain":chain,
        //     "name": name,
        //     "version": version,
        //     "args": args,
        //     "endorsement":endorsement[0],
        // }
        console.log("---+更新合约---",formData)
        const headers = getAuthorizedHeader();
        axios.post(`${ROOT_URL}/chaincode/upgrade`, formData, {headers: { 'content-type': 'application/x-www-form-urlencoded', ...headers }})
            .then(function(response) {
                if(response.data.status == 1) {// success
                    dispatch({ type: UPDATE_CHAINCODE, payload:response });
                    callback();
                } else {// fail
                    callback(response.data.message);
                }
            })
            .catch(function(err) {
                dispatch(requestError(err.message));
                callback(err.message);
            });
    }
}

/**
 * 执行合约
 * @param name
 * @param version
 * @param args
 * @param callback(err)   :  callback when http request response. if(success) err=null ; else err=[error message].
 */
export function executeChainCode({ chain, name, version, func,args }, callback) {
  console.log(`executeChainCode: ${chain}, ${name}, ${version}, ${func},${args}`);

  // let arry =[];
  //  for(let i =0;i < args.length;i++)
  //  {
  //      let aaa = args[i]
  //
  //      console.log( "--------------",aaa)
  //      arry.push(aaa)
  //
  //
  //  }

    // console.log("++++array+++++++",arry)
  return function(dispatch) {
      let formData = {
          "chain":chain,
          "name": name,
          "version": version,
          "function":func,
          "args": args
      }
      console.log("---执行合约---",formData)
      const headers = getAuthorizedHeader();
    axios.post(`${ROOT_URL}/chaincode/tx`, formData, {headers: { ...headers }})
      .then(response => {
        if(response.data.status == 1 && response.data.data.success == 1) {// success
          const data = response.data.data;
          dispatch({ type: EXECUTE_CHAINCODE, payload:response });
          callback();
        } else {// fail
          callback(response.data.message);
        }

      })
      .catch( err => {
        dispatch(requestError(err.message));
        callback(err.message);
      } );
  }
}

/**
 * 查询合约
 * @param name
 * @param version
 * @param args
 * @param callback(err)   :  callback when http request response. if(success) err=null ; else err=[error message].
 */
export function fetchChainCodeInfo({ chain, name, version,func, args }, callback) {
    console.log(`executeChainCode: ${chain}, ${name}, ${version}, ${func},${args}`);
  return function(dispatch) {

      let formData = {
          "chain":chain,
          "name": name,
          "version": version,
          "function":func,
          "args": args
      }
      console.log("---查询合约---",formData)
      const headers = getAuthorizedHeader();
    axios.post(`${ROOT_URL}/chaincode/query`, formData,{headers: { 'content-type': 'application/json', ...headers }})
      .then(response => {
        dispatch({ type: FETCH_CHAINCODE_INFO, payload:response })
        if(response.data.status == 1) {
          callback();
        } else {//fail
          callback(response.data.message);
        }
      })
      .catch( err => {
        dispatch(requestError(err.message))
        callback(err.message);
      });
  }
}