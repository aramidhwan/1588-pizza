<template>

<v-data-table
    :headers="headers"
    :items="myPage"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'MyPage',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "myPageId", value: "myPageId" },
            { text: "orderId", value: "orderId" },
            { text: "pizzaNm", value: "pizzaNm" },
            { text: "qty", value: "qty" },
            { text: "status", value: "status" },
            { text: "orderDt", value: "orderDt" },
            { text: "customerId", value: "customerId" },
        ],
        myPage : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/mypages')

      this.myPage = temp.data._embedded.mypages;

    },
    methods: {
    }
  }
</script>

