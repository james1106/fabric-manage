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
  FETCH_CHAINCODE_LIST
} from '../actions/types';

const INITIAL_STATE = { all: null };

export default function(state = INITIAL_STATE, action) {
  switch(action.type) {
    case FETCH_CHAINCODE_LIST:
      return { ...state, all:action.payload.data.data||[] };
  }

  return state;
}