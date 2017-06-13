import React, { Component } from 'react';
import { connect } from 'react-redux';
import { fetchPeerList, switchPeerStatus } from '../actions/peer';
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
import AddPeer from './peer_add';

class PeerList extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isModalOpen: false,
      isAddModalOpen : false,
      isDetailModalOpen : false,
      actionSuccess: null,
      actionResult: '',
      selectedIndex: null
    };

  }

  componentWillMount() {
    this.props.fetchPeerList(function(err){
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
        <td>{row.id}</td>
        <td>{row.endpoint}</td>
        <td>{row.status}</td>
        <td>
          <button className={`btn btn-sm btn-success margin-r-5 hidden`}
                  onClick={this.handleStopClick.bind(this, row.id, 1)}>启动</button>
          <button className={`btn btn-sm btn-danger margin-r-5 hidden`}
                  onClick={this.handleStopClick.bind(this, row.id, 0)}>停止</button>
          <button className={`btn btn-sm btn-warning hidden margin-r-5`}
                  onClick={this.handleDetailClick.bind(this, idx)}>详情</button>
          <Link className="btn btn-sm btn-warning margin-r-5" to={`/peer/${row.id}`}>详情</Link>
        </td>
      </tr>);
    });
  }

  handleDetailClick(index) {
    this.setState({ selectedIndex: index, isDetailModalOpen: true });
  }


  handleStopClick(id, up) {
    this.props.switchPeerStatus(id, up?1:0, success => {
      console.log(success);
      if(success) {//操作成功
        this.props.fetchPeerList();
      }
      this.setState({ isModalOpen: true ,actionSuccess:success, actionResult: success?'操作成功!':'操作失败' });
    });
  };

  handleAddClick() {
    this.setState({isAddModalOpen: true});
  }

  hideAddModal = () => {
    this.setState({ isAddModalOpen : false });
  };

  hideDetailModal = () => {
    this.setState({ isDetailModalOpen : false });
  };

  addCallback(err) {
    if(!err){
      this.props.fetchPeerList();
      this.setState({isAddModalOpen : false, isModalOpen: true ,actionSuccess:true, actionResult:'节点添加成功!' });
    }
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
                  <h3 className="box-title">节点</h3>
                  <button className="btn btn-success pull-right" onClick={this.handleAddClick.bind(this)}><i className="fa fa-plus"></i> 添加节点</button>
                </div>
                <div className="box-body table-responsive no-padding">
                  <table className="table table-bordered table-hover">
                    <tbody>
                    <tr>
                      <th>ID</th>
                      <th>EndPoint</th>
                      <th>状态</th>
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

        <Modal isOpen={this.state.isAddModalOpen} onRequestHide={this.hideAddModal}>
          <ModalHeader>
            <ModalClose onClick={this.hideAddModal}/>
            <ModalTitle>添加节点</ModalTitle>
          </ModalHeader>
          <ModalBody>
            <AddPeer addCallback={this.addCallback.bind(this)}/>
          </ModalBody>
          <ModalFooter>
          </ModalFooter>
        </Modal>

      </div>)
  }
}

function mapStateToProps(state) {
  return {
    all: state.peer.all
  };
}

export default connect(mapStateToProps, { fetchPeerList, switchPeerStatus })(PeerList);