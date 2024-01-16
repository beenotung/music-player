import { Capacitor } from '@capacitor/core'
import { Directory, Encoding, Filesystem } from '@capacitor/filesystem'

declare var statusNode: HTMLElement
declare var platformNode: HTMLElement
declare var dirTemplate: HTMLElement
declare var dirList: HTMLInputElement
declare var messageNode: HTMLElement

statusNode.textContent = 'status: loaded script'
platformNode.textContent = Capacitor.getPlatform()

for (let key in Directory) {
  let node = dirTemplate.cloneNode(true) as HTMLElement
  let button = node.querySelector('button')!
  node.id = ''
  button.textContent = key
  button.onclick = async function () {
    let result = await Filesystem.readdir({
      directory: (Directory as any)[key],
      path: '.',
    })
    console.log('result:', result)
  }
  dirList.appendChild(node)
}
