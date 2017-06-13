/**
 *
 *
 * Author: Jun
 * Date: 12/06/2017
 *
 */

import React, { Component } from 'react';
import { connect } from 'react-redux';
import { fetchChainList } from '../actions/chain';
import { Link } from 'react-router-dom';
import Moment from 'react-moment';
import {
  Modal,
  ModalHeader,
  ModalTitle,
  ModalClose,
  ModalBody,
  ModalFooter
} from 'react-modal-bootstrap';
import AddChain from './chain_add';
import ChainAddPeer from './chain_add_peer';

class ChainList extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isModalOpen: false,
      isAddModalOpen : false,
      isAddPeerModalOpen : false,
      actionSuccess: null,
      actionResult: '',
      selectedIndex: null
    };

  }

  componentWillMount() {
    this.props.fetchChainList(function(err){
      if(err) alert(err);
    });
  }

  hideModal = () => {
    this.setState({
      isModalOpen: false
    });
  };

  renderRows() {
    return this.props.all.map((row, idx) => {
      return (<tr key={idx}>
        <td>{row.name}</td>
        <td><Moment locale="zh-cn" format="lll">{row.createtime}</Moment></td>
        <td>{row.affiliation}</td>
        <td>{row.height}</td>
        <td>{row.hash}</td>
        <td>{row.next}</td>
        <td>
          <Link className="btn btn-sm btn-warning margin-r-5" to={`/chain/${row.name}`}>详情</Link>
          <button className="btn btn-sm btn-warning margin-r-5" onClick={this.handleAddPeer.bind(this, idx)}>添加节点</button>
        </td>
      </tr>);
    });
  }

  render() {
    if(this.props.all===null) {
      return <div><section className="content"><h1>Loading...</h1></section></div>
    }

    let selectedChainName = null;
    if(this.state.selectedIndex != null) {
      selectedChainName = this.props.all[this.state.selectedIndex].name;
    }

    return (
      <div>
        <section className="content-header"><h1></h1></section>
        <section className="content">
          <div className="row">
            <div className="col-xs-12">
              <div className="box box-info">
                <div className="box-header">
                  <h3 className="box-title">链</h3>
                  <button className="btn btn-success pull-right" onClick={this.handleAddClick.bind(this)}><i className="fa fa-plus"></i> 添加链</button>
                </div>
                <div className="box-body table-responsive no-padding">
                  <table className="table table-bordered table-hover">
                    <tbody>
                    <tr>
                      <th>名称</th>
                      <th>创建时间</th>
                      <th>机构</th>
                      <th>链高度</th>
                      <th>Hash</th>
                      <th>Next</th>
                      <th>操作</th>
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

        <Modal isOpen={this.state.isModalOpen} onRequestHide={this.hideModal}>
          <ModalHeader>
            <ModalClose onClick={this.hideModal}/>
            <ModalTitle>结果</ModalTitle>
          </ModalHeader>
          <ModalBody>
            <p className={this.state.actionSuccess?'text-green':'text-red'}>{this.state.actionSuccess?'操作成功!':'操作失败'}</p>
          </ModalBody>
          <ModalFooter>
            <button className='btn btn-default' onClick={this.hideModal}>
              关闭
            </button>
          </ModalFooter>
        </Modal>

        <Modal isOpen={this.state.isAddModalOpen} onRequestHide={this.hideAddModal.bind(this)}>
          <ModalHeader>
            <ModalClose onClick={this.hideAddModal.bind(this)}/>
            <ModalTitle>添加链</ModalTitle>
          </ModalHeader>
          <ModalBody>
            <AddChain addCallback={this.addCallback.bind(this)}/>
          </ModalBody>
          <ModalFooter>
          </ModalFooter>
        </Modal>

        <Modal isOpen={this.state.isAddPeerModalOpen} onRequestHide={this.hideAddPeerModal.bind(this)}>
          <ModalHeader>
            <ModalClose onClick={this.hideAddPeerModal.bind(this)}/>
            <ModalTitle>节点入链</ModalTitle>
          </ModalHeader>
          <ModalBody>
            <ChainAddPeer chainName={selectedChainName} addCallback={this.addPeerCallback.bind(this)}/>
          </ModalBody>
          <ModalFooter>
          </ModalFooter>
        </Modal>

      </div>)
  }


  handleAddClick() {
    this.setState({isAddModalOpen: true});
  }

  hideAddModal = () => {
    this.setState({ isAddModalOpen : false });
  };

  addCallback(err) {
    if(!err){
      this.props.fetchChainList();
      this.setState({isAddModalOpen : false, isModalOpen: true ,actionSuccess:true, actionResult:'链添加成功!' });
    }
  }

  handleAddPeer(index) {
    this.setState({ selectedIndex: index, isAddPeerModalOpen: true });
  }

  hideAddPeerModal = () => {
    this.setState({ isAddPeerModalOpen : false });
  };

  addPeerCallback(err) {
    if(!err){
      this.props.fetchChainList();
      this.setState({isAddPeerModalOpen : false, isModalOpen: true ,actionSuccess:true, actionResult:'节点入链成功!' });
    }
  }

}

function mapStateToProps(state) {
  return {
    all: state.chain.chainList
  };
}

export default connect(mapStateToProps, { fetchChainList })(ChainList);