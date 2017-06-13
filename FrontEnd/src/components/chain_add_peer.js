/**
 * oxchain
 *
 *
 * Author: Jun
 * Date: 13/06/2017
 *
 */


import React, { Component } from 'react';
import { connect } from 'react-redux';
import { fetchPeerList } from '../actions/peer';
import { attachPeer } from '../actions/chain';
import { Link } from 'react-router-dom';

class ChainAddPeer extends Component {
  constructor(props) {
    super(props);
    this.state = {
      actionSuccess: null,
      actionResult: '',
      selectedId: null,
      error:null,
      spin:false
    };

  }

  componentWillMount() {
    this.props.fetchPeerList(function(err){
      if(err) alert(err);
    });
  }

  handleSelect({ id }, event) {
    //console.log(event.target.checked, id);
    this.setState({selectedId:id});
  }

  handleAddClick() {
    const chainName = this.props.chainName;
    const peerId = this.state.selectedId;

    if(this.state.selectedId===null || !chainName) {
      return;
    }
    this.setState({ spin:true });
    this.props.attachPeer( {chainName, peerId}, err => {
      this.setState({ error: err ? err : null, spin:false });
      if(typeof this.props.addCallback === 'function')  this.props.addCallback(err);
    });
  }

  renderAlert() {
    if (this.state.error) {
      return (
        <div className="alert alert-danger alert-dismissable">
          {this.state.error}
        </div>
      );
    }
  }

  renderRows() {
    return this.props.all.map((row, idx) => {
      return (<tr key={idx}>
        <td>{row.id}</td>
        <td>{row.endpoint}</td>
        <td>{row.status}</td>
        <td>
          <input type="radio" label={row.id} key={row.id}
                 onChange={this.handleSelect.bind(this, row)}
                 checked={this.state.selectedId == row.id}/>
        </td>
      </tr>);
    });
  }

  render() {
    if(this.props.all===null) {
      return <div><section className="content"><h1>Loading...</h1></section></div>
    }

    //console.log(this.props.chainName);
    return (
      <div>
        <section className="content-header"><h1></h1></section>
        <section className="content">
          <div className="row">
            <div className="col-xs-12">
              {this.renderAlert()}
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
          </div>
          <div className="row">
            <div className="col-xs-12">
              <button className="btn btn-success pull-right" onClick={this.handleAddClick.bind(this)}><i className={`fa fa-spinner fa-spin ${this.state.spin?'':'hidden'}`}></i> 确定 </button>
            </div>
          </div>
        </section>

      </div>)
  }
}

function mapStateToProps(state) {
  return {
    all: state.peer.all
  };
}

export default connect(mapStateToProps, { fetchPeerList, attachPeer })(ChainAddPeer);