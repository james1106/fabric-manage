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
import ChaincodeInit from './chaincode_init';
import ChaincodeExecute from './chaincode_execute';
import ChaincodeQuery from './chaincode_query';
import UpdateChainCode from './chaincode_update';



class ChainCodeList extends Component {
  constructor() {
    super();
    this.state = {
      isModalOpen: false,
      isAddModalOpen : false,
      isInstallModalOpen : false,
      isInitModalOpen : false,
      isExecuteModalOpen : false,
      isQueryModalOpen: false,
        isUpdateModalOpen:false,
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

  //////// upload ////////

  handleAddClick() {
    this.setState({isAddModalOpen: true});
  }

  hideAddModal = () => {
    this.setState({ isAddModalOpen : false });
  };

  uploadCallback(err) {
    if(!err){
      this.props.fetchChainCodeList();
      this.setState({isAddModalOpen : false, isModalOpen: true ,actionSuccess:true, actionResult:'合约上传成功!' });
    }
  }

  //////// install ////////

  handleInstallClick(row) {
    this.setState({isInstallModalOpen: true, selectedItem: row});
  }

  hideInstallModal = () => {
    this.setState({ isInstallModalOpen : false, selectedItem: null });
  };

  installCallback(err) {
    if(!err){
      this.props.fetchChainCodeList();
      this.setState({isInstallModalOpen : false, isModalOpen: true ,actionSuccess:true, actionResult:'部署成功!' });
    }
  }

  //////// init ////////

  handleInitClick(row) {
    this.setState({isInitModalOpen: true, selectedItem: row});
  }

  hideInitModal = () => {
    this.setState({ isInitModalOpen : false, selectedItem: null });
  };

  initCallback(err) {
    if(!err){
      this.props.fetchChainCodeList();
      this.setState({isInitModalOpen : false, isModalOpen: true ,actionSuccess:true, actionResult:'初始化成功!' });
    }
  }


  ///////Update/////

    handleUpdateClick(row) {
        this.setState({isUpdateModalOpen: true, selectedItem: row});
    }

    hideUpdateModal = () => {
        this.setState({ isUpdateModalOpen : false, selectedItem: null });
    };

    UpdateCallback(err) {
        if(!err){
            this.props.fetchChainCodeList();
            this.setState({isUpdateModalOpen : false, isModalOpen: true ,actionSuccess:true, actionResult:'更新成功!' });
        }
    }


  //////// execute ////////

  handleExecuteClick(row) {
    this.setState({isExecuteModalOpen: true, selectedItem: row});
  }

  hideExecuteModal = () => {
    this.setState({ isExecuteModalOpen : false, selectedItem: null });
  };

  executeCallback(err) {
    if(!err){
      this.props.fetchChainCodeList();
      this.setState({isExecuteModalOpen : false, isModalOpen: true ,actionSuccess:true, actionResult:'合约执行成功!' });
    }
  }

  //////// query ////////

  handleQueryClick(row) {
    this.setState({isQueryModalOpen: true, selectedItem: row});
  }

  hideQueryModal = () => {
    this.setState({ isQueryModalOpen : false, selectedItem: null });
  };


  renderRows() {
    return this.props.all.map((row, idx) => {
      let buttons = [<button className='btn btn-sm btn-default margin-r-5' onClick={this.handleInstallClick.bind(this, row)} key="1">部署</button>];
      if(row.installed && row.installed.length>0) {//已经部署
        buttons.push(<button className='btn btn-sm btn-default margin-r-5' onClick={this.handleInitClick.bind(this, row)} key="2">初始化</button>);
        //if(row.instantiated && row.instantiated.length>0) {//已经初始化

            buttons.push(<button className='btn btn-sm btn-default margin-r-5' onClick={this.handleUpdateClick.bind(this, row)}  key="3">更新</button>);

          buttons.push(<button className='btn btn-sm btn-default margin-r-5' onClick={this.handleExecuteClick.bind(this, row)}  key="4">执行</button>);
          buttons.push(<button className='btn btn-sm btn-default margin-r-5' onClick={this.handleQueryClick.bind(this, row)}  key="5">查询</button>);
        //}
      }
      return (<tr key={idx}>
        <td>{row.name}</td>
        <td>{row.version}</td>
        <td>{row.lang}</td>
        <td>
          {buttons}
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

        {/* Result Modal */}
        <Modal isOpen={this.state.isModalOpen} onRequestHide={this.hideModal.bind(this)}>
          <ModalHeader>
            <ModalClose onClick={this.hideModal.bind(this)}/>
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

        {/* Upload Modal */}
        <Modal isOpen={this.state.isAddModalOpen} onRequestHide={this.hideAddModal.bind(this)}>
          <ModalHeader>
            <ModalClose onClick={this.hideAddModal.bind(this)}/>
            <ModalTitle>上传合约</ModalTitle>
          </ModalHeader>
          <ModalBody>
            <ChaincodeUpload addCallback={this.uploadCallback.bind(this)}/>
          </ModalBody>
          <ModalFooter>
          </ModalFooter>
        </Modal>

        {/* Install Modal */}
        <Modal isOpen={this.state.isInstallModalOpen} onRequestHide={this.hideInstallModal.bind(this)}>
          <ModalHeader>
            <ModalClose onClick={this.hideInstallModal.bind(this)}/>
            <ModalTitle>部署合约</ModalTitle>
          </ModalHeader>
          <ModalBody>
            <ChaincodeInstall actionCallback={this.installCallback.bind(this)} selectedItem={this.state.selectedItem}/>
          </ModalBody>
          <ModalFooter>
          </ModalFooter>
        </Modal>

        {/* Init Modal */}
        <Modal isOpen={this.state.isInitModalOpen} onRequestHide={this.hideInitModal.bind(this)}>
          <ModalHeader>
            <ModalClose onClick={this.hideInitModal.bind(this)}/>
            <ModalTitle>初始化合约</ModalTitle>
          </ModalHeader>
          <ModalBody>
            <ChaincodeInit actionCallback={this.initCallback.bind(this)} selectedItem={this.state.selectedItem}/>
          </ModalBody>
          <ModalFooter>
          </ModalFooter>
        </Modal>

          {/* Update Modal*/}
        <Modal isOpen={this.state.isUpdateModalOpen} onRequestHide={this.hideUpdateModal.bind(this)}>
          <ModalHeader>
            <ModalClose onClick={this.hideUpdateModal.bind(this)}/>
            <ModalTitle>更新合约</ModalTitle>
          </ModalHeader>
          <ModalBody>
            <UpdateChainCode actionCallback={this.UpdateCallback.bind(this)} selectedItem={this.state.selectedItem}/>
          </ModalBody>
          <ModalFooter>
          </ModalFooter>
        </Modal>


        {/* Execute Modal */}
        <Modal isOpen={this.state.isExecuteModalOpen} onRequestHide={this.hideExecuteModal.bind(this)}>
          <ModalHeader>
            <ModalClose onClick={this.hideExecuteModal.bind(this)}/>
            <ModalTitle>执行合约</ModalTitle>
          </ModalHeader>
          <ModalBody>
            <ChaincodeExecute actionCallback={this.executeCallback.bind(this)} selectedItem={this.state.selectedItem}/>
          </ModalBody>
          <ModalFooter>
          </ModalFooter>
        </Modal>

        {/* Query Modal */}
        <Modal isOpen={this.state.isQueryModalOpen} onRequestHide={this.hideQueryModal.bind(this)}>
          <ModalHeader>
            <ModalClose onClick={this.hideQueryModal.bind(this)}/>
            <ModalTitle>查询合约</ModalTitle>
          </ModalHeader>
          <ModalBody>
            <ChaincodeQuery selectedItem={this.state.selectedItem}/>
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