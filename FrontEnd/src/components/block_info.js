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
import { fetchBlockInfo } from '../actions/chain';

class BlockInfo extends Component {
  constructor(props) {
    super(props);
    this.state = { error:null, spin:false };
  }

  componentWillMount() {
    if(this.props.match.params.blockid!==null && this.props.match.params.chainname!==null)
      this.props.fetchBlockInfo(this.props.match.params.chainname, this.props.match.params.blockid);
  }

  renderDataList(dataList) {
    if(!dataList || dataList.length<1) return <div></div>;

    return dataList.map((row, idx) => {
      return (
        <dl className="dl-horizontal" key={idx}>
          <dt>txid</dt>
          <dd>{row.txid}</dd>
          <dt>type</dt>
          <dd>{row.type}</dd>
          <dt>version</dt>
          <dd>{row.version}</dd>
          <dt>epoch</dt>
          <dd>{row.epoch}</dd>
          <dt>channel</dt>
          <dd>{row.channel}</dd>
          <dt>nonce</dt>
          <dd>{row.nonce}</dd>
          <dt>signature</dt>
          <dd>{row.signature}</dd>
        </dl>
      );
    });
  }

  render() {
    if(this.props.block == null){
      return <div><section className="content"><h1>Loading...</h1></section></div>
    }

    const {number, size, previous, hash, datalist} = this.props.block;

    return (
      <div>
        <section className="content">
          <div className="row">
            <div className="col-xs-12">
              <div className="box box-info">
                <div className="box-header">
                  <h3 className="box-title">区块详情</h3>
                </div>
                <div className="box-body table-responsive">
                  <div className="panel panel-default">
                    <div className="panel-heading">基本信息</div>
                    <div className="panel-body">
                      <dl className="dl-horizontal">
                        <dt>区块号</dt>
                        <dd>{number}</dd>
                        <dt>区块大小</dt>
                        <dd>{size}</dd>
                        <dt>区块哈希值</dt>
                        <dd>{hash}</dd>
                        <dt>上一区块哈希值</dt>
                        <dd>{previous}</dd>
                      </dl>
                    </div>
                  </div>
                  <div className="panel panel-default">
                    <div className="panel-heading">List</div>
                    <div className="panel-body">
                      {this.renderDataList(datalist)}
                    </div>
                  </div>

                </div>
                <div className="box-footer clearfix">
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    );
  }
}

function mapStateToProps(state) {
  return {
    block: state.chain.block
  };
}
export default connect(mapStateToProps, { fetchBlockInfo })(BlockInfo);