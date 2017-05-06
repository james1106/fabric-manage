import { combineReducers } from 'redux';
import { reducer as formReducer } from 'redux-form'
import authReducer from './auth_reducer';
import peerReducer from './peer_reduicer';
import userReducer from './user_reduicer';
import chainReducer from './chain_reduicer';

const rootReducer = combineReducers({
  form: formReducer,
  auth: authReducer,
  peer: peerReducer,
  user: userReducer,
  chain: chainReducer
});

export default rootReducer;
