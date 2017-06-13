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
import _ from 'lodash';
import Moment from 'react-moment';
import {
  Modal,
  ModalHeader,
  ModalTitle,
  ModalClose,
  ModalBody,
  ModalFooter
} from 'react-modal-bootstrap';
import { fetchEventHubList,fetchPeerList } from '../actions/peer';
import EnrollPeer from './peer_enroll';

class PeerDetail extends Component {
  constructor(props) {
    super(props);
    this.state = {
      error:null,
      spin:false,
      isEnrollModalOpen: false
    };
  }

  componentWillMount() {
    this.props.fetchPeerList();
    //this.props.fetchEventHubList();
  }

  handleEnrollClick() {
    this.setState({ isEnrollModalOpen: true });
  }

  enrollCallback(err) {
    if(!err){
      this.setState({isEnrollModalOpen : false });
    }
  }

  hideEnrollModal = () => {
    this.setState({ isEnrollModalOpen : false });
  };

  renderChainCodes(chaincodes) {
    if(!chaincodes || chaincodes.length<1) return <div>没有合约</div>;

    return chaincodes.map((row, idx) => {
      return (
        <dl className="dl-horizontal" key={idx}>
          <dt>Name</dt>
          <dd>{row.name}</dd>
          <dt>Input</dt>
          <dd>{row.input}</dd>
          <dt>Path</dt>
          <dd>{row.path}</dd>
          <dt>Version</dt>
          <dd>{row.version}</dd>
          <dt>Error</dt>
          <dd>{row.error}</dd>
        </dl>
      );
    });
  }

  renderEventHub(peerID) {
    if(!this.props.eventhub) return <div>该节点没有关联Eventhub</div>;

    const eventhub = _.find(this.props.eventhub, { 'id': peerID });
    if(!eventhub) return <div>该节点没有关联Eventhub</div>;

    return (
      <dl className="dl-horizontal">
        <dt>在线时间</dt>
        <dd><Moment locale="zh-cn" format="lll">{eventhub.online}</Moment></dd>
        <dt>离线时间</dt>
        <dd><Moment locale="zh-cn" format="lll">{eventhub.offline}</Moment></dd>
        <dt>连接状态</dt>
        <dd>{eventhub.connected?'已连接':'未连接'}</dd>
        <dt>EndPoint</dt>
        <dd>{eventhub.endpoint}</dd>
        <dt>上次连接时间</dt>
        <dd><Moment locale="zh-cn" format="lll">{eventhub.lastattempt}</Moment></dd>
      </dl>
    );
  }

  renderCert() {
    if(!this.props.cert) return <button className="btn btn-success btn-sm" onClick={this.handleEnrollClick.bind(this)}><i className="fa fa-eye"></i> 查看证书</button>;

    const cert = this.props.cert;
    return (
      <dl className="dl-horizontal">
        <dt>CA</dt>
        <dd>{cert.ca}</dd>
        <dt>uri</dt>
        <dd>{cert.uri}</dd>
        <dt>私钥</dt>
        <dd style={{wordWrap:'break-word'}}>{cert.privateKey}</dd>
        <dt>证书</dt>
        <dd>{cert.certificate}</dd>
      </dl>
    );
  }

  render() {
    if(this.props.all == null){
      return <div><section className="content"><h1>Loading...</h1></section></div>
    }

    const  peerID  = this.props.match.params.id;
    const item = _.find(this.props.all, { 'id': peerID });

    return (
      <div>
        <section className="content">
          <div className="row">
            <div className="col-xs-12">
              <div className="box box-info">
                <div className="box-header">
                  <h3 className="box-title">节点详情</h3>
                </div>
                <div className="box-body table-responsive">
                  <div className="panel panel-default">
                    <div className="panel-heading">基本信息</div>
                    <div className="panel-body">
                      <dl className="dl-horizontal">
                        <dt>ID</dt>
                        <dd>{item.id}</dd>
                        <dt>EndPoint</dt>
                        <dd>{item.endpoint}</dd>
                        <dt>状态</dt>
                        <dd>{item.status}</dd>
                        <dt>Chains</dt>
                        <dd>{item.chains.join(', ')}</dd>
                      </dl>
                    </div>
                  </div>

                  <div className="panel panel-default">
                    <div className="panel-heading">合约</div>
                    <div className="panel-body">
                      {this.renderChainCodes(item.chaincodes)}
                    </div>
                  </div>

                  <div className="panel panel-default hidden">
                    <div className="panel-heading">EventHub</div>
                    <div className="panel-body">
                      {this.renderEventHub(item.id)}
                    </div>
                  </div>

                  <div className="panel panel-default">
                    <div className="panel-heading">节点证书
                    </div>
                    <div className="panel-body">
                      {this.renderCert()}
                    </div>
                  </div>

                </div>
                <div className="box-footer clearfix">
                </div>
              </div>
            </div>
          </div>
        </section>

        <Modal isOpen={this.state.isEnrollModalOpen} onRequestHide={this.hideEnrollModal.bind(this)}>
          <ModalHeader>
            <ModalClose onClick={this.hideEnrollModal.bind(this)}/>
            <ModalTitle>查看节点证书</ModalTitle>
          </ModalHeader>
          <ModalBody>
            <EnrollPeer peerId={peerID} affiliation={item.affiliation} callback={this.enrollCallback.bind(this)}/>
          </ModalBody>
          <ModalFooter>
          </ModalFooter>
        </Modal>
      </div>
    );
  }
}

function mapStateToProps(state) {
  return {
    all: state.peer.all,
    eventhub: state.peer.eventhub,
    cert: state.peer.cert
  };
}
export default connect(mapStateToProps, { fetchEventHubList, fetchPeerList })(PeerDetail);