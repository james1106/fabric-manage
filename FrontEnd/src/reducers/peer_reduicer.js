import {
  REQUEST_SUCCESS,
  REQUEST_ERROR,
  FETCH_PEER_LIST,
  FETCH_PEER_STATUS
} from '../actions/types';

const INITIAL_STATE = { all: [], status:null };

export default function(state = INITIAL_STATE, action) {
  switch(action.type) {
    case FETCH_PEER_LIST:
      return { ...state, all:action.payload.data.data };
    case FETCH_PEER_STATUS:
      return { ...state, status:action.payload.data.data };
  }

  return state;
}