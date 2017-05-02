import React, { Component } from 'react';
import { connect } from 'react-redux';
import { fetchUserList, disableUser } from '../actions/user';
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

class UserList extends Component {
  constructor() {
    super();
    this.state = {
      isModalOpen: false,
      actionSuccess: null
    };

  }

  componentWillMount() {

    this.props.fetchUserList();
  }

  hideModal = () => {
    this.setState({
      isModalOpen: false
    });
  };

  renderRows() {
    return this.props.all.map((row, idx) => {
      return (<tr key={idx}>
        <td>{row.username}</td>
        <td>{row.affiliation}</td>
        <td>
          <button className="btn btn-sm btn-danger margin-r-5"
                  onClick={this.handleStopClick.bind(this, row)}>禁用</button>
        </td>
      </tr>);
    });
  }

  handleStopClick({username}) {
    this.props.disableUser(username, success => {
      console.log(success);
      if(success) {//操作成功
        this.props.fetchUserList();
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
                <div className="box-header"><h3 className="box-title">用户</h3></div>
                <div className="box-body table-responsive no-padding">
                  <table className="table table-bordered table-hover">
                    <tbody>
                    <tr>
                      <th>ID</th>
                      <th>从属</th>
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
    all: state.user.all
  };
}

export default connect(mapStateToProps, { fetchUserList, disableUser })(UserList);