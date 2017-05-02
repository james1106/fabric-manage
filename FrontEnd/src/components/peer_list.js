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

class PeerList extends Component {
  constructor() {
    super();
    this.state = {
      isModalOpen: false,
      actionSuccess: null
    };

  }

  componentWillMount() {

    this.props.fetchPeerList();
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
        <td>{row.ip}</td>
        <td>{row.port}</td>
        <td>{row.block_height}</td>
        <td>{row.up?'运行':'停止'}</td>
        <td>
          <button className={`btn btn-sm ${row.up?'btn-danger':'btn-success'} margin-r-5`}
                  onClick={this.handleStopClick.bind(this, row)}>{row.up?'停止':'启动'}</button>
          <Link className="btn btn-sm btn-default" to={`/peer/${row.id}/status`}>状态</Link>
        </td>
      </tr>);
    });
  }

  handleStopClick({id, up}) {
    this.props.switchPeerStatus(id, up?0:1, success => {
      console.log(success);
      if(success) {//操作成功
        this.props.fetchPeerList();
      }
      this.setState({ isModalOpen: true ,actionSuccess:success });
    });
  };

  render() {
    if(this.props.all.length<1) {
      return <div><section className="content-header"><h1>Loading...</h1></section></div>
    }

    return (
      <div>
        <section className="content-header"><h1></h1></section>
        <section className="content">
          <div className="row">
            <div className="col-xs-12">
              <div className="box box-info">
                <div className="box-header"><h3 className="box-title">节点</h3></div>
                <div className="box-body table-responsive no-padding">
                  <table className="table table-bordered table-hover">
                    <tbody>
                    <tr>
                      <th>ID</th>
                      <th>IP</th>
                      <th>Port</th>
                      <th>区块高度</th>
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
      </div>)
  }
}

function mapStateToProps(state) {
  return {
    all: state.peer.all
  };
}

export default connect(mapStateToProps, { fetchPeerList, switchPeerStatus })(PeerList);