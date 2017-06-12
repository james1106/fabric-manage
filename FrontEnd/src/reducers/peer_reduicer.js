import {
  REQUEST_SUCCESS,
  REQUEST_ERROR,
  FETCH_PEER_LIST,
  FETCH_PEER_STATUS,
  FETCH_EVENTHUB_LIST,
  ENROLL_PEER_SUCCESS
} from '../actions/types';

const INITIAL_STATE = { all: null, status:null, eventhub:null, err:null, cert:null };

export default function(state = INITIAL_STATE, action) {
  switch(action.type) {
    case FETCH_PEER_LIST:
      return { ...state, all:action.payload.data.data };
    case FETCH_EVENTHUB_LIST:
      return { ...state, eventhub:action.payload.data.data };
    case FETCH_PEER_STATUS:
      return { ...state, status:action.payload.data.data };
    case ENROLL_PEER_SUCCESS:
      return { ...state, cert: action.payload.data.data };
    case REQUEST_ERROR:
      return { ...state, err:action.payload.data}
  }

  return state;
}