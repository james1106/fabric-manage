import axios from 'axios';
import {
  ROOT_URL,
  REQUEST_SUCCESS,
  REQUEST_ERROR,
  FETCH_PEER_LIST,
  FETCH_PEER_STATUS,
  ADD_PEER_SUCCESS,
  ADD_PEER_ERROR,
  FETCH_EVENTHUB_LIST
} from './types';

export function requestError(error) {
  return {
    type: REQUEST_ERROR,
    payload: error
  };
}

//节点列表
export function fetchPeerList() {
  return function(dispatch) {
    axios.get(`${ROOT_URL}/peer`)
      .then(response => dispatch({ type: FETCH_PEER_LIST, payload:response }))
      .catch( err => dispatch(requestError(err.message)) );
  }
}

//EventHub列表
export function fetchEventHubList() {
  return function(dispatch) {
    axios.get(`${ROOT_URL}/peer/eventhub`)
      .then(response => dispatch({ type: FETCH_EVENTHUB_LIST, payload:response }))
      .catch( err => dispatch(requestError(err.message)) );
  }
}

//节点状态
export function fetchPeerStatus(id) {
  return function(dispatch) {
    axios.get(`${ROOT_URL}/peer/${id}/status`)
      .then(response => dispatch({ type: FETCH_PEER_STATUS, payload:response }))
      .catch( err => dispatch(requestError(err.message)) );
  }
}

//更新状态
export function switchPeerStatus(id, action, callback) {
  return function(dispatch) {
    axios.put(`${ROOT_URL}/peer/${id}/status?action=${action}`)
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
export function addPeer({ id, endpoint, eventhub }, callback) {
  console.log(`addPeer: ${id}, ${endpoint}, ${eventhub}`);
  return function(dispatch) {
    axios.post(`${ROOT_URL}/peer`, { id, endpoint, eventhub })
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