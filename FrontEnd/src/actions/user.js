import axios from 'axios';
import {
  ROOT_URL,
  REQUEST_SUCCESS,
  REQUEST_ERROR,
  FETCH_USER_LIST,
  ADD_USER_SUCCESS,
  ADD_USER_ERROR,
  getAuthorizedHeader
} from './types';

export function requestError(error) {
  return {
    type: REQUEST_ERROR,
    payload: error
  };
}

//节点列表
export function fetchUserList(callback) {
  return function(dispatch) {
    axios.get(`${ROOT_URL}/user`, { headers: getAuthorizedHeader() })
      .then(response => dispatch({ type: FETCH_USER_LIST, payload:response }))
      .catch( err => {
        //console.log(err.response.status)
        dispatch(requestError(err.message))
        callback(err.message);
      } );
  }
}

//禁用用户
export function disableUser(id, reason, callback) {
  return function(dispatch) {
    axios.put(`${ROOT_URL}/user/${id}?action=0&reason=${reason}`, null, { headers: getAuthorizedHeader() })
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


//注册用户
export function addUser({ username, password }, callback) {
  console.log(`addUser: ${username}, ${password}`);
  return function(dispatch) {
    axios.post(`${ROOT_URL}/user`, { username, password }, { headers: getAuthorizedHeader() })
      .then(response => {

        if(response.data.status == 1) {// success
          dispatch({ type: ADD_USER_SUCCESS, payload:response });
          callback();
        } else {// fail
          dispatch({ type: ADD_USER_ERROR, payload:response.data.message });
          callback(response.data.message);
        }

      })
      .catch( err => {
        dispatch(requestError(err.message));
        callback(err.message);
      } );
  }
}