import React, { Component } from 'react';
import { connect } from 'react-redux';
import { fetchPeerStatus } from '../actions/peer';
import { Link } from 'react-router-dom';
import Moment from 'react-moment';

class PeerStatus extends Component {

  componentWillMount() {
    this.props.fetchPeerStatus(this.props.match.params.id);
  }

  render() {
    if(!this.props.status) {
      return <div><section className="content-header"><h1>Loading...</h1></section></div>
    }

    const { uptime, up, ip, block_height } = this.props.status;
    return (
      <div>
        <section className="content-header"><h1></h1></section>
        <section className="content">
          <div className="row">
            <div className="col-xs-6 col-xs-offset-3">
              <div className="box box-info">
                <div className="box-header text-center"><h3 className="box-title">节点状态</h3></div>
                <div className="box-body table-responsive no-padding">
                  <table className="table table-bordered table-hover">
                    <tbody>
                    <tr>
                      <th>ID</th>
                      <td>{this.props.match.params.id}</td>
                    </tr>
                    <tr>
                      <th>运行时间</th>
                      <td>{uptime}</td>
                    </tr>
                    <tr>
                      <th>状态</th>
                      <td>{up?'运行':'停止'}</td>
                    </tr>
                    <tr>
                      <th>IP</th>
                      <td>{ip}</td>
                    </tr>
                    <tr>
                      <th>区块高度</th>
                      <td>{block_height}</td>
                    </tr>
                    </tbody>
                  </table>
                </div>
                <div className="box-footer clearfix text-center">
                  <Link to="/peer" className="btn btn-default">返回</Link>
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
    status: state.peer.status
  };
}

export default connect(mapStateToProps, { fetchPeerStatus })(PeerStatus);