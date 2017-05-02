import axios from 'axios';
import {
  ROOT_URL,
  AUTH_USER,
  UNAUTH_USER,
  AUTH_ERROR
} from './types';


// 登录
export function signinAction(props, callback) {
  return function(dispatch) {
    //TODO: using GET for test only
    //axios.post(`${ROOT_URL}/signin`, { username, password })
    axios.get(`${ROOT_URL}/signin`)
      .then(response => {

        if(response.data.status == 1) {//auth success
          // - Save the JWT token
          /*
           localStorage.setItem('token', response.data.token);
           localStorage.setItem('user', JSON.stringify(response.data.user));
           localStorage.setItem('username', response.data.user.username);
           */
          localStorage.setItem('token', response.data.data.id);
          localStorage.setItem('user', JSON.stringify(response.data.data));
          localStorage.setItem('username', response.data.data.name);

          dispatch({type: AUTH_USER});
          // - redirect to the route '/'
          callback();
        } else {//auth fail
          dispatch(authError(response.data.message));
        }

      })
      .catch(() => {
        // If request is bad...
        // - Show an error to the user
        dispatch(authError('Bad Login'));
      });
  }
}

export function authError(error) {
  return {
    type: AUTH_ERROR,
    payload: error
  };
}

// 登出
export function signoutUser() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  localStorage.removeItem('username');

  return { type: UNAUTH_USER };
}
