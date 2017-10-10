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
  ADD_CHAIN,
  FETCH_BLOCK_LIST,
  FETCH_BLOCK_INFO,
  FETCH_CHAIN_INFO,
  FETCH_CHAIN_LIST,
  getAuthorizedHeader
} from './types';

export function requestError(error) {
  return {
    type: REQUEST_ERROR,
    payload: error
  };
}


//链列表
export function fetchChainList(callback) {
  return function(dispatch) {
    axios.get(`${ROOT_URL}/chain`, { headers: getAuthorizedHeader() })
      .then(response => dispatch({ type: FETCH_CHAIN_LIST, payload:response }))
      .catch( err => {
        dispatch(requestError(err.message));
        if(typeof callback === 'function') callback(err.message);
      } );
  }
}

export function addChain({ chainname, config }, callback) {
  //console.log(`addChain: ${chainname}, ${endorsement}`);

  return function(dispatch) {
    let formData = new FormData();
    formData.append('chainname', chainname);
    formData.append('config', config[0]);
    const headers = getAuthorizedHeader();
    axios.post(`${ROOT_URL}/chain`, formData, {headers: { 'content-type': 'multipart/form-data', ...headers }})
      .then(function(response) {
        if(response.data.status == 1) {// success
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

//Chain详情
export function fetchChainInfo(name) {
  return function(dispatch) {
    axios.get(`${ROOT_URL}/chain/${name}`,{ headers: getAuthorizedHeader() })
      .then(response => dispatch({ type: FETCH_CHAIN_INFO, payload:response }))
      .catch( err => dispatch(requestError(err.message)) );
  }
}

//Block列表
export function fetchBlockList(name) {
  return function(dispatch) {
    axios.get(`${ROOT_URL}/chain/${name}/block`, { headers: getAuthorizedHeader() })
      .then(response => dispatch({ type: FETCH_BLOCK_LIST, payload:response }))
      .catch( err => dispatch(requestError(err.message)) );
  }
}

//Block详情
export function fetchBlockInfo(chainName, id) {
  return function(dispatch) {
    axios.get(`${ROOT_URL}/chain/${chainName}/block?number=${id}`, { headers: getAuthorizedHeader() })
      .then(response => dispatch({ type: FETCH_BLOCK_INFO, payload:response }))
      .catch( err => dispatch(requestError(err.message)) );
  }
}

export function attachPeer({ chainName, peerId }, callback) {

  return function(dispatch) {
    axios.post(`${ROOT_URL}/chain/${chainName}/peer?peer=${peerId}`, null, { headers: getAuthorizedHeader() })
      .then(function(response) {
        if(response.data.status == 1) {// success
          callback();
        } else {// fail
          callback(response.data.message);
        }
      })
      .catch(err => callback(err.message) );

  }
}