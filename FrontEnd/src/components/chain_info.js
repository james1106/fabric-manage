/**
 * oxchain ivoice app
 *
 *
 * Author: Jun
 * Email: iyakexi@gmail.com
 * Date: 05/05/2017
 *
 */

import React, { Component } from 'react';
import { connect } from 'react-redux';
import { fetchChainInfo } from '../actions/chain';
import { Link } from 'react-router-dom';

class ChainInfo extends Component {

  componentWillMount() {
    this.props.fetchChainInfo(this.props.chainName);
  }

  render() {
    if(this.props.chain===null) {
      return <div><section className="content"><h3>Loading...</h3></section></div>
    }
    const {hash, next, height} = this.props.chain;

    return (
      <div>
        <section className="content-header"><h1></h1></section>
        <section className="content">
          <div className="row">
            <div className="col-xs-12">
              <div className="box box-info">
                <div className="box-header">
                  <h3 className="box-title">区块链当前状态</h3>
                  <div className="box-tools pull-right">
                    <button type="button" className="btn btn-box-tool" data-widget="collapse"><i className="fa fa-minus"></i></button>
                  </div>
                </div>
                <div className="box-body table-responsive">
                  <dl>
                    <dt>区块高度:</dt>
                    <dd>{height}</dd>
                    <dt>当前哈希值:</dt>
                    <dd>{hash}</dd>
                    <dt>Next:</dt>
                    <dd>{next}</dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
        </section>

      </div>)
  }
}

function mapStateToProps(state) {
  return {
    chain: state.chain.chain
  };
}

export default connect(mapStateToProps, { fetchChainInfo })(ChainInfo);