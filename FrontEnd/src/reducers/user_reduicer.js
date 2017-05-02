import {
  REQUEST_SUCCESS,
  REQUEST_ERROR,
  FETCH_USER_LIST
} from '../actions/types';

const INITIAL_STATE = { all: [] };

export default function(state = INITIAL_STATE, action) {
  switch(action.type) {
    case FETCH_USER_LIST:
      return { ...state, all:action.payload.data.data };
  }

  return state;
}