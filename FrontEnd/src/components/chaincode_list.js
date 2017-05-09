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
import { fetchChainCodeList } from '../actions/chaincode';
import { Link } from 'react-router-dom';
import {
  Modal,
  ModalHeader,
  ModalTitle,
  ModalClose,
  ModalBody,
  ModalFooter
} from 'react-modal-bootstrap';
import ChaincodeUpload from './chaincode_upload';
import ChaincodeInstall from './chaincode_install';

class ChainCodeList extends Component {
  constructor() {
    super();
    this.state = {
      isModalOpen: false,
      isAddModalOpen : false,
      isInstallModalOpen : false,
      actionSuccess: null,
      actionResult: '',
      selectedItem: null
    };

  }

  componentWillMount() {
    this.props.fetchChainCodeList();
  }

  hideModal = () => {
    this.setState({
      isModalOpen: false
    });
  };

  handleAddClick() {
    this.setState({isAddModalOpen: true});
  }

  hideAddModal = () => {
    this.setState({ isAddModalOpen : false });
  };

  handleInstallClick(row) {
    this.setState({isInstallModalOpen: true, selectedItem: row});
  }

  hideInstallModal = () => {
    this.setState({ isInstallModalOpen : false, selectedItem: null });
  };

  uploadCallback(err) {
    if(!err){
      this.props.fetchChainCodeList();
      this.setState({isAddModalOpen : false, isModalOpen: true ,actionSuccess:true, actionResult:'添加成功!' });
    }
  }

  installCallback(err) {
    if(!err){
      this.props.fetchChainCodeList();
      this.setState({isInstallModalOpen : false, isModalOpen: true ,actionSuccess:true, actionResult:'部署成功!' });
    }
  }


  renderRows() {
    return this.props.all.map((row, idx) => {
      return (<tr key={idx}>
        <td>{row.name}</td>
        <td>{row.version}</td>
        <td>{row.lang}</td>
        <td>
          <button className='btn btn-sm btn-default margin-r-5'>执行</button>
          <button className='btn btn-sm btn-default margin-r-5'>初始化</button>
          <button className='btn btn-sm btn-default margin-r-5' onClick={this.handleInstallClick.bind(this, row)}>部署</button>
        </td>
      </tr>);
    });
  }

  render() {
    if(this.props.all===null) {
      return <div><section className="content"><h1>Loading...</h1></section></div>
    }

    return (
      <div>
        <section className="content-header"><h1></h1></section>
        <section className="content">
          <div className="row">
            <div className="col-xs-12">
              <div className="box box-info">
                <div className="box-header">
                  <h3 className="box-title">合约</h3>
                  <button className="btn btn-success pull-right hidden" onClick={this.handleAddClick.bind(this)}><i className="fa fa-plus"></i> 添加合约</button>
                  <Link to="/chaincode/add" className="btn btn-success pull-right hidden"><i className="fa fa-plus"></i> 添加合约</Link>
                  <button className="btn btn-success pull-right" onClick={this.handleAddClick.bind(this)}><i className="fa fa-plus"></i> 上传合约</button>
                </div>
                <div className="box-body table-responsive no-padding">
                  <table className="table table-bordered table-hover">
                    <tbody>
                    <tr>
                      <th>名称</th>
                      <th>版本</th>
                      <th>语言</th>
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
            <p className={this.state.actionSuccess?'text-green':'text-red'}>
              {this.state.actionResult}
            </p>
          </ModalBody>
          <ModalFooter>
          </ModalFooter>
        </Modal>

        <Modal isOpen={this.state.isAddModalOpen} onRequestHide={this.hideAddModal}>
          <ModalHeader>
            <ModalClose onClick={this.hideAddModal}/>
            <ModalTitle>上传合约</ModalTitle>
          </ModalHeader>
          <ModalBody>
            <ChaincodeUpload addCallback={this.uploadCallback.bind(this)}/>
          </ModalBody>
          <ModalFooter>
          </ModalFooter>
        </Modal>

        <Modal isOpen={this.state.isInstallModalOpen} onRequestHide={this.hideInstallModal}>
          <ModalHeader>
            <ModalClose onClick={this.hideInstallModal}/>
            <ModalTitle>部署合约</ModalTitle>
          </ModalHeader>
          <ModalBody>
            <ChaincodeInstall actionCallback={this.installCallback.bind(this)} selectedItem={this.state.selectedItem}/>
          </ModalBody>
          <ModalFooter>
          </ModalFooter>
        </Modal>

      </div>)
  }
}

function mapStateToProps(state) {
  return {
    all: state.chainCode.all
  };
}

export default connect(mapStateToProps, { fetchChainCodeList })(ChainCodeList);