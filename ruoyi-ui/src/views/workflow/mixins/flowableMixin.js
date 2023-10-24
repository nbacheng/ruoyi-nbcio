import Vue from 'vue'

export const flowableMixin = {
  components: {
  },
  data(){
    return {
      customformList: [],
      formQueryParams:{
        pageNum: 1,
        pageSize: 1000,
      },
    }
  },
  created() {
  },
  computed:{
    /*所有的自定义业务流程表单，组件化注册，在此维护*/
    allFormComponent:function(){
      return [
          {
            text:'单表示例',
            routeName: '@/views/workflow/demo/wf',
            component: () => import('@/views/workflow/demo/wf'),
            businessTable:'wf_demo'
          },
          /*{
            text:'主子表示例',
            routeName:'@/views/workflow/demo/modules/CesOrderMainForm',
            component:() => import(`@/views/workflow/demo/modules/CesOrderMainForm`),
            businessTable:'ces_order_main'
          }*/
      ]
    }
  },
  methods:{
    getFormComponent(routeName){
      return _.find(this.allFormComponent,{routeName:routeName})||{};
    },

    handleTableChange(pagination, filters, sorter) {
      //分页、排序、筛选变化时触发
      //TODO 筛选
      if (Object.keys(sorter).length > 0) {
        this.isorter.column = sorter.field;
        this.isorter.order = "ascend" == sorter.order ? "asc" : "desc"
      }
      this.ipagination = pagination;
      // this.loadData();
    },
    millsToTime(mills) {
      if (!mills) {
        return "";
      }
      let s = mills / 1000;
      if (s < 60) {
        return s.toFixed(0) + " 秒"
      }
      let m = s / 60;
      if (m < 60) {
        return m.toFixed(0) + " 分钟"
      }
      let h = m / 60;
      if (h < 24) {
        return h.toFixed(0) + " 小时"
      }
      let d = h / 24;
      if (d < 30) {
        return d.toFixed(0) + " 天"
      }
      let month = d / 30
      if (month < 12) {
        return month.toFixed(0) + " 个月"
      }
      let year = month / 12
      return year.toFixed(0) + " 年"

    },
  }

}
