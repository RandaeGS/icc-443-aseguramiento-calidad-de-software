import Notify from 'simple-notify'
import 'simple-notify/dist/simple-notify.css'

const createNotification = (status, title, text) => {
  return new Notify({
    status,
    title,
    text,
    effect: 'fade',
    speed: 300,
    showIcon: true,
    showCloseButton: true,
    autoclose: true,
    autotimeout: 3000,
    type: 'filled',
    position: 'x-center bottom',
  })
}

export default createNotification
