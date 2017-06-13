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
  FETCH_CHAIN_INFO,
  FETCH_CHAIN_LIST,
  FETCH_BLOCK_LIST,
  FETCH_BLOCK_INFO
} from '../actions/types';

const INITIAL_STATE = { chainList: null, chain: null, blocks: null, block: null };

export default function(state = INITIAL_STATE, action) {
  switch(action.type) {
    case FETCH_CHAIN_LIST:
      return { ...state, chainList:action.payload.data.data };
    case FETCH_CHAIN_INFO:
      return { ...state, chain:action.payload.data.data };
    case FETCH_BLOCK_LIST:
      return { ...state, blocks:action.payload.data.data };
    case FETCH_BLOCK_INFO:
      return { ...state, block:action.payload.data.data };
  }

  return state;
}