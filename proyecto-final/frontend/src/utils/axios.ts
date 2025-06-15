import axios from 'axios'

const axiosInstance = axios.create({
  timeout: 2000,
})

export default axiosInstance
