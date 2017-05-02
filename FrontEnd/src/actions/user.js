import axios from 'axios';
import {
  ROOT_URL,
  REQUEST_SUCCESS,
  REQUEST_ERROR,
  FETCH_USER_LIST
} from './types';

export function requestError(error) {
  return {
    type: REQUEST_ERROR,
    payload: error
  };
}

//节点列表
export function fetchUserList() {
  return function(dispatch) {
    axios.get(`${ROOT_URL}/user`)
      .then(response => dispatch({ type: FETCH_USER_LIST, payload:response }))
      .catch( response => dispatch(requestError(response.data.error)) );
  }
}

//禁用用户
export function disableUser(id, callback) {
  return function(dispatch) {
    //axios.put(`${ROOT_URL}/user/${id}?action=0)
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