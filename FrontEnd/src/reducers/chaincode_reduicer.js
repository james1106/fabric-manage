/**
 * oxchain ivoice app
 *
 *
 * Author: Jun
 * Email: iyakexi@gmail.com
 * Date: 05/05/2017
 *
 */

import {
  REQUEST_SUCCESS,
  REQUEST_ERROR,
  FETCH_CHAINCODE_LIST,
  FETCH_CHAINCODE_INFO
} from '../actions/types';

const INITIAL_STATE = { all: null, item: null };

export default function(state = INITIAL_STATE, action) {
  switch(action.type) {
    case FETCH_CHAINCODE_LIST:
      return { ...state, all:action.payload.data.data||[] };
    case FETCH_CHAINCODE_INFO:
      return { ...state, item:action.payload.data.data };
  }

  return state;
}