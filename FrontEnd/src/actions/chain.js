/**
 * oxchain ivoice app
 *
 *
 * Author: Jun
 * Email: iyakexi@gmail.com
 * Date: 05/05/2017
 *
 */

import axios from 'axios';
import {
  ROOT_URL,
  REQUEST_SUCCESS,
  REQUEST_ERROR,
  FETCH_BLOCK_LIST,
  FETCH_BLOCK_INFO,
  FETCH_CHAIN_INFO
} from './types';

export function requestError(error) {
  return {
    type: REQUEST_ERROR,
    payload: error
  };
}

//Chain详情
export function fetchChainInfo() {
  return function(dispatch) {
    axios.get(`${ROOT_URL}/chain`)
      .then(response => dispatch({ type: FETCH_CHAIN_INFO, payload:response }))
      .catch( err => dispatch(requestError(err.message)) );
  }
}

//Block列表
export function fetchBlockList() {
  return function(dispatch) {
    axios.get(`${ROOT_URL}/chain/block`)
      .then(response => dispatch({ type: FETCH_BLOCK_LIST, payload:response }))
      .catch( err => dispatch(requestError(err.message)) );
  }
}

//Block详情
export function fetchBlockInfo(id) {
  return function(dispatch) {
    axios.get(`${ROOT_URL}/chain/block?number=${id}`)
      .then(response => dispatch({ type: FETCH_BLOCK_INFO, payload:response }))
      .catch( err => dispatch(requestError(err.message)) );
  }
}