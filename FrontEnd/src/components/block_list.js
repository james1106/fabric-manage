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
import { fetchBlockList, fetchBlockInfo } from '../actions/chain';
import { Link } from 'react-router-dom';
import {
  Modal,
  ModalHeader,
  ModalTitle,
  ModalClose,
  ModalBody,
  ModalFooter
} from 'react-modal-bootstrap';

class BlockList extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isDetailModalOpen : false,
      selectedId: null
    };

  }

  componentWillMount() {
    this.props.fetchBlockList();
  }

  renderRows() {
    return this.props.blocks.map((row, idx) => {
      return (<tr key={idx}>
        <td>{row.number}</td>
        <td>{row.size}</td>
        <td>{row.hash}</td>
        <td>{row.previous}</td>
        <td>
          <button className={`btn btn-sm btn-warning margin-r-5 hidden`}
                  onClick={this.handleDetailClick.bind(this, row.number)}>详情</button>
          <Link className="btn btn-sm btn-warning" to={`/chain/block/${row.number}`}>详情</Link>
        </td>
      </tr>);
    });
  }

  handleDetailClick(index) {
    console.log(index);
    this.props.fetchBlockInfo(index);
    this.setState({ isDetailModalOpen: true });
  }

  render() {

    if(this.props.blocks===null) {
      return <div><section className="content"><h3>Loading...</h3></section></div>
    }

    return (
      <div>
        <section className="content-header"><h1></h1></section>
        <section className="content">
          <div className="row">
            <div className="col-xs-12">
              <div className="box box-info">
                <div className="box-header">
                  <h3 className="box-title">最新区块</h3>
                  <div className="box-tools pull-right">
                    <button type="button" className="btn btn-box-tool" data-widget="collapse"><i className="fa fa-minus"></i></button>
                  </div>
                </div>
                <div className="box-body table-responsive no-padding">
                  <table className="table table-bordered table-hover multi-line-table">
                    <tbody>
                    <tr>
                      <th>区块号</th>
                      <th>区块大小</th>
                      <th>区块哈希值</th>
                      <th>上一区块哈希值</th>
                      <th></th>
                    </tr>
                    { this.renderRows() }
                    </tbody>
                  </table>
                </div>
                <div className="box-footer clearfix">
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
    blocks: state.chain.blocks
  };
}

export default connect(mapStateToProps, { fetchBlockList, fetchBlockInfo })(BlockList);