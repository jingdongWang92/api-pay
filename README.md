

## 地图工具方案购买流程

1. 用户选择对应的方案, 选择支付方式, 点击确认下单(调用创建订单接口)
2. 后台生成一条未支付的订单记录, 同时返回订单的信息
3. 成功创建订单后根据用户选择的支付方式调用支付宝或者微信支付接口
4. 用户支付成功后，后台收到微信或者支付宝的付款成功通知后，更新该订单的状态以及用户的方案信息


## 支付相关api
### 1. 创建订单
- 请求路径 `/orders`
- 请求方式 `POST`
- 请求参数说明
<pre>
{
  "user": "586c6ebacbda800001c6d2cb",
  "trade_amount": 1,
  "pay_way": "alipay",
  "plan": "59f14733c5ee742cd2200d21"
}
</pre>
响应
<pre>
{
  "error": false,
  "message": "",
  "payload": {
    id: "", //id即为订单号
    ...
  }
}
</pre>

### 2.  微信扫码支付
- 请求路径 `/wxpay`
- 请求方式 `POST`
- 请求参数说明
<pre>
{
  "out_trade_no": "201811080001",  
  "trade_amount": 1, //单位: 元
  "body": "地图工具企业版",
  "subject": "地图工具企业版",
  "product_id": "123456789",
}
</pre>
响应
<pre>
{
    "error": false,
    "message": "",
    "payload": {
        "nonce_str": "6y3Ey3HlioBgm0nI",
        "code_url": "weixin://wxpay/bizpayurl?pr=hG0kt1L", //二维码链接
        "appid": "wx100d268a6a1246e7",
        "sign": "0C2F456BC3C77318BC16B533D100BFEA",
        "trade_type": "NATIVE",
        "return_msg": "OK",
        "result_code": "SUCCESS",
        "mch_id": "1460124702",
        "return_code": "SUCCESS",
        "prepay_id": "wx201711081620433c8447f4600696133669"
    },
    "meta": null
}
</pre>

### 3. 支付宝网页支付
- 请求路径 `/alipay/page-pay`
- 请求方式 `GET`
- 请求参数说明
<pre>
{
  "out_trade_no": "201811080001",  
  "trade_amount": 1, //单位: 元
  "body": "地图工具企业版",
  "subject": "地图工具企业版"
}
</pre>
