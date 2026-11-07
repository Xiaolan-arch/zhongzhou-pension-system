export default defineAppConfig({
  pages: [
    'pages/home/index',
    'pages/service/index',
    'pages/bill/index',
    'pages/mine/index',
    'pages/login/index',
    'pages/elder/index',
    'pages/report/index',
    'pages/reportDetail/index',
    'pages/billDetail/index',
    'pages/deposit/index',
    'pages/message/index',
    'pages/invoice/index',
    'pages/serviceDetail/index',
    'pages/visit/index',
    // 公开浏览（游客也可访问）
    'pages/public/serviceItems/index',
    'pages/public/rooms/index',
    'pages/public/roomDetail/index',
    // 入住申请（需登录）
    'pages/checkinApply/index',
    'pages/checkinList/index',
    'pages/checkinDetail/index',
    // 绑定/解绑老人
    'pages/bind/index',
    'pages/bindList/index',
    // 体征查询
    'pages/health/index',
    'pages/healthTrend/index',
    // 床位详情
    'pages/elderBed/index',
    // 服务项目详情 + 下单
    'pages/serviceItemDetail/index',
    // 日常照护 / 家属服务
    'pages/leave/index',
    'pages/care/index',
    'pages/feedback/index',
    'pages/invoiceApply/index',
    // AI 智能客服
    'pages/chat/index'
  ],
  window: {
    backgroundTextStyle: 'light',
    navigationBarBackgroundColor: '#2fa36b',
    navigationBarTitleText: '中州颐养',
    navigationBarTextStyle: 'white',
    backgroundColor: '#f4f7f5'
  },
  tabBar: {
    color: '#8a94a6',
    selectedColor: '#2fa36b',
    backgroundColor: '#ffffff',
    borderStyle: 'white',
    list: [
      {
        pagePath: 'pages/home/index',
        text: '首页',
        iconPath: 'assets/tabbar/home.png',
        selectedIconPath: 'assets/tabbar/home-selected.png'
      },
      {
        pagePath: 'pages/service/index',
        text: '服务',
        iconPath: 'assets/tabbar/service.png',
        selectedIconPath: 'assets/tabbar/service-selected.png'
      },
      {
        pagePath: 'pages/bill/index',
        text: '账单',
        iconPath: 'assets/tabbar/bill.png',
        selectedIconPath: 'assets/tabbar/bill-selected.png'
      },
      {
        pagePath: 'pages/mine/index',
        text: '我的',
        iconPath: 'assets/tabbar/mine.png',
        selectedIconPath: 'assets/tabbar/mine-selected.png'
      }
    ]
  }
})
