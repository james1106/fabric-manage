/**
 * oxchain ivoice app
 *
 *
 * Author: Jun
 * Email: iyakexi@gmail.com
 * Date: 03/05/2017
 *
 */


import React, { Component } from 'react';
import { Field, reduxForm } from 'redux-form';
import { connect } from 'react-redux';
import { addPeer } from '../actions/peer'

class AddPeer extends Component {
  constructor(props) {
    super(props);
    this.state = { error:null, spin:false };
  }

  handleFormSubmit({ id, endpoint, eventhub }) {
    if(id && endpoint && eventhub) {
      this.setState({ spin:true });
      this.props.addPeer({ id, endpoint, eventhub }, err => {
        this.setState({ error: err ? err : null, spin:false });
        this.props.addCallback(err);
      });
    }
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

  renderField({ input, label, type, meta: { touched, error } }) {
    return (
      <div className={`form-group has-feedback ${touched && error ? 'has-error' : ''}`}>
        <input {...input} placeholder={label} type={type} className="form-control"/>
        <div className="help-block with-errors">{touched && error ? error : ''}</div>
      </div>
    )}

  render() {
    const { handleSubmit} = this.props;

    return (
      <div>
        <div className="login-box">
          <div className="login-logo">
          </div>
          <div className="login-box-body">
            <p className="login-box-msg" style={{fontSize: 24+'px'}}>添加节点</p>
            {this.renderAlert()}
            <form className="form-signin" onSubmit={handleSubmit(this.handleFormSubmit.bind(this))}>
              <Field name="id" component={this.renderField} type="text"  label="节点ID" />
              <Field name="endpoint" component={this.renderField} type="text"  label="EndPoint" />
              <Field name="eventhub" component={this.renderField} type="text" label="EventHub" />
              <div className="row">
                <div className="col-xs-8">
                </div>
                <div className="col-xs-4">
                  <button type="submit" className="btn btn-primary btn-block btn-flat"><i className={`fa fa-spinner fa-spin ${this.state.spin?'':'hidden'}`}></i> 添加 </button>
                </div>
              </div>

            </form>
          </div>
        </div>
      </div>
    );
  }
}


const validate = values => {
  const errors = {};

  if(!values.id) {
    errors.id = '节点ID不能为空';
  }

  if(!values.endpoint) {
    errors.endpoint = 'EndPoint不能为空';
  }

  if(!values.eventhub) {
    errors.eventhub = 'EventHub不能为空';
  }

  return errors
};


const reduxAddPeerForm = reduxForm({
  form: 'AddPeerForm',
  validate
})(AddPeer);

export default connect(null, { addPeer })(reduxAddPeerForm);