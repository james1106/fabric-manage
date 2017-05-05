import {
  REQUEST_SUCCESS,
  REQUEST_ERROR,
  FETCH_PEER_LIST,
  FETCH_PEER_STATUS,
  FETCH_EVENTHUB_LIST
} from '../actions/types';

const INITIAL_STATE = { all: null, status:null, eventhub:null };

export default function(state = INITIAL_STATE, action) {
  switch(action.type) {
    case FETCH_PEER_LIST:
      return { ...state, all:action.payload.data.data };
    case FETCH_EVENTHUB_LIST:
      return { ...state, eventhub:action.payload.data.data };
    case FETCH_PEER_STATUS:
      return { ...state, status:action.payload.data.data };
  }

  return state;
}