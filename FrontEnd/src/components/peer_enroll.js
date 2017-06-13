/**
 * oxchain ivoice app
 *
 *
 * Author: Jun
 * Date: 12/06/2017
 *
 */


import React, { Component } from 'react';
import { Field, reduxForm } from 'redux-form';
import { connect } from 'react-redux';
import { enrollPeer } from '../actions/peer'

class EnrollPeerForm extends Component {
  constructor(props) {
    super(props);
    this.state = {
      errorMessage: null,
      data:null
    };
  }

  handleFormSubmit({ password }) {
    const peerId = this.props.peerId;
    const affiliation = this.props.affiliation;
    if(password)
      this.props.enrollPeer({ peerId, affiliation, password }, (err)=>{
        this.setState({
          errorMessage: err
        });
        if(!err) {
          this.props.callback(err);
        }
      });
  }

  renderAlert() {
    if (this.state.errorMessage) {
      return (
        <div className="alert alert-danger">
          {this.state.errorMessage}
        </div>
      );
    }
  }

  renderField({ input, label, type, meta: { touched, error } }) {
    return (
      <div className={`form-group has-feedback ${touched && error ? 'has-error' : ''}`}>
        <label className="col-sm-2 control-label">{label}</label>
        <div className="col-sm-10">
          <input {...input} placeholder={label} type={type} className="form-control"/>
          <div className="help-block with-errors">{touched && error ? error : ''}</div>
        </div>
      </div>
    )
  }

  render() {
    const { handleSubmit} = this.props;
    const peerId = this.props.peerId;
    const affiliation = this.props.affiliation;

    return (
      <div>
        <section className="content">
          <div>
            {this.renderAlert()}

            <form className="form-horizontal" onSubmit={handleSubmit(this.handleFormSubmit.bind(this))}>
              <div className='form-group'>
                <label className="col-sm-2 control-label">节点ID</label>
                <div className="col-sm-10">
                  <input type="text" className="form-control" value={peerId} disabled="true"/>
                </div>
              </div>
              <div className='form-group'>
                <label className="col-sm-2 control-label">机构</label>
                <div className="col-sm-10">
                  <input type="text" className="form-control" value={affiliation} disabled="true"/>
                </div>
              </div>
              <Field name="password" component={this.renderField} type="password" label="节点密码"  />
              <div className="row">
                <div className="col-xs-8">
                </div>
                <div className="col-xs-4">
                  <button type="submit" className="btn btn-primary btn-block btn-flat">确认</button>
                </div>
              </div>
            </form>
          </div>
        </section>
      </div>
    );
  }
}


const validate = values => {
  const errors = {};

  if(!values.password) {
    errors.password = 'password required'
  }

  return errors
};

function mapStateToProps(state) {
  return {
  };
}

const reduxEnrollPeerForm = reduxForm({
  form: 'EnrollPeerForm',
  validate
})(EnrollPeerForm);

export default connect(null, { enrollPeer })(reduxEnrollPeerForm);