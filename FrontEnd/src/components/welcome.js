import React from 'react';
import Moment from 'react-moment';

export default () => {
  return (
  <div className="welcomebgc">
      <section className="content-header"><h1></h1></section>
      <section className="content">
        <div className="row">
          <div className="md-col-12 text-center welcometitle">
            <h1>欢迎使用区块链管理系统</h1>
            <div>现在时间是: <Moment locale="zh-cn" format="lll"></Moment></div>
            <img className="logoimg"  src="http://www.ziyungufen.com/static/ziyungufen/image/logo.png" alt=""/>
              <img className="gykt" src="../../screenshot/gykt.png" alt=""/>
           <div className="world">
             <img className="worldimg" src="../../screenshot/world.png" alt=""/>
           </div>
          </div>
        </div>
      </section>
  </div>);
};