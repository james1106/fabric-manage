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
  EXECUTE_CHAINCODE
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
    axios.get(`${ROOT_URL}/chaincode`)
      .then(response => dispatch({ type: FETCH_CHAINCODE_LIST, payload:response }))
      .catch( err => dispatch(requestError(err.message)) );
  }
}


//上传合约
export function uploadChainCode({ name, version, lang, chaincode }, callback) {
  console.log(`uploadChainCode: ${name}, ${version}, ${lang}, ${chaincode}`);
  console.log(chaincode)

  return function(dispatch) {
    let formData = new FormData();
    formData.append('name', name)
    formData.append('version', version)
    formData.append('lang', lang)
    formData.append('chaincode', chaincode[0])
    const config = {
      headers: { 'content-type': 'multipart/form-data' }
    }
    axios.post(`${ROOT_URL}/chaincode/file`, formData, config)
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


//部署合约
export function installChainCode({ name, version, lang, peers }, callback) {
  console.log(`installChainCode: ${name}, ${version}, ${lang}, ${peers}`);

  return function(dispatch) {
    axios.post(`${ROOT_URL}/chaincode/install/${name}?version=${version}&lang=${lang}&peers=${peers.join()}`)
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