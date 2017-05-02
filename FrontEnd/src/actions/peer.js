import axios from 'axios';
import {
  ROOT_URL,
  REQUEST_SUCCESS,
  REQUEST_ERROR,
  FETCH_PEER_LIST,
  FETCH_PEER_STATUS
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
      .catch( response => dispatch(requestError(response.data.error)) );
  }
}

//节点状态
export function fetchPeerStatus(id) {
  return function(dispatch) {
    //axios.get(`${ROOT_URL}/peer/${id}/status`)
    axios.get(`${ROOT_URL}/peer-${id}-status`)
      .then(response => dispatch({ type: FETCH_PEER_STATUS, payload:response }))
      .catch( response => dispatch(requestError(response.data.error)) );
  }
}

//更新状态
export function switchPeerStatus(id, action, callback) {
  return function(dispatch) {
    //axios.put(`${ROOT_URL}/peer/${id}/status?action=${action})
    axios.get(`${ROOT_URL}/action-success`)
      .then(response => {
        dispatch({ type: REQUEST_SUCCESS, payload:response });
        callback(!!response.data.status);
      })
      .catch( response => {
        dispatch(requestError(response.data.error));
        callback(false);
      } );
  }
}