import axios from 'axios';
import {
  ROOT_URL,
  REQUEST_SUCCESS,
  REQUEST_ERROR,
  FETCH_PEER_LIST,
  FETCH_PEER_STATUS,
  ADD_PEER_SUCCESS,
  ADD_PEER_ERROR,
  FETCH_EVENTHUB_LIST,
  ENROLL_PEER_SUCCESS,
  getAuthorizedHeader
} from './types';

export function requestError(error) {
  return {
    type: REQUEST_ERROR,
    payload: error
  };
}

//节点列表
export function fetchPeerList(callback) {
  return function(dispatch) {
    axios.get(`${ROOT_URL}/peer`, { headers: getAuthorizedHeader() })
      .then(response => dispatch({ type: FETCH_PEER_LIST, payload:response }))
      .catch( err => {
        dispatch(requestError(err.message));
        if(typeof callback === 'function') callback(err.message);
      } );
  }
}

//EventHub列表
//deprecated @ 2016-06-12
export function fetchEventHubList() {
  return function(dispatch) {
    axios.get(`${ROOT_URL}/peer/eventhub`, { headers: getAuthorizedHeader() })
      .then(response => dispatch({ type: FETCH_EVENTHUB_LIST, payload:response }))
      .catch( err => dispatch(requestError(err.message)) );
  }
}

//节点状态
export function fetchPeerStatus(id) {
  return function(dispatch) {
    axios.get(`${ROOT_URL}/peer/${id}/status`, { headers: getAuthorizedHeader() })
      .then(response => dispatch({ type: FETCH_PEER_STATUS, payload:response }))
      .catch( err => dispatch(requestError(err.message)) );
  }
}

//更新状态
//deprecated @ 2017-06-12
export function switchPeerStatus(id, action, callback) {
  return function(dispatch) {
    axios.put(`${ROOT_URL}/peer/${id}/status?action=${action}`, { headers: getAuthorizedHeader() })
      .then(response => {
        dispatch({ type: REQUEST_SUCCESS, payload:response });
        callback(!!response.data.status);
      })
      .catch( err => {
        dispatch(requestError(err.message));
        callback(false);
      } );
  }
}

//添加节点
export function addPeer({ id, endpoint, eventhub, password }, callback) {
  //console.log(`addPeer: ${id}, ${endpoint}, ${eventhub}`);
  return function(dispatch) {
    axios.post(`${ROOT_URL}/peer`, { id, endpoint, eventhub, password }, { headers: getAuthorizedHeader() })
      .then(response => {

        if(response.data.status == 1) {// success
          dispatch({ type: ADD_PEER_SUCCESS, payload:response });
          callback();
        } else {// fail
          dispatch({ type: ADD_PEER_ERROR, payload:response.data.message });
          callback(response.data.message);
        }

      })
      .catch(err => {
        dispatch(requestError(err.message));
        callback(err.message);
      });
  }
}

//登录节点, 获取节点证书
export function enrollPeer({ peerId, affiliation, password }, callback) {
  return function(dispatch) {
    axios.post(`${ROOT_URL}/peer/enrollment`, { id:peerId, affiliation, password })
      .then(response => {
        if(response.data.status == 1) {//auth success
          dispatch({type: ENROLL_PEER_SUCCESS, payload: response });
          callback(null, response.data.data);
        } else {//auth fail
          callback(response.data.message);
        }
      })
      .catch((err) => {
        callback(err.message);
      });
  }
}
