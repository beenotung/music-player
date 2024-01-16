import { Capacitor } from '@capacitor/core'
import { Directory, Encoding, Filesystem } from '@capacitor/filesystem'
import { format_byte, format_long_short_time } from '@beenotung/tslib/format'

declare var statusNode: HTMLElement
declare var platformNode: HTMLElement
declare var audioNode: HTMLAudioElement
declare var videoNode: HTMLAudioElement
declare var dirList: HTMLInputElement
declare var fileList: HTMLInputElement
declare var messageNode: HTMLElement

statusNode.textContent = 'status: loaded script'
platformNode.textContent = Capacitor.getPlatform()

audioNode.hidden = true
videoNode.hidden = true

let dirTemplate = dirList.children[0]
dirTemplate.remove()
for (let key in Directory) {
  let dirNode = dirTemplate.cloneNode(true) as HTMLElement
  let button = dirNode.querySelector('button')!
  button.textContent = key
  button.onclick = async function () {
    let status = await Filesystem.requestPermissions()
    messageNode.textContent = 'Permission: ' + status.publicStorage
    dirList
      .querySelectorAll('.dir')
      .forEach(e => e.classList.remove('selected'))
    dirNode.classList.add('selected')
    let directory = (Directory as any)[key]
    showDir(directory, '.')
  }
  dirList.appendChild(dirNode)
}

async function showDir(directory: Directory, dirPath: string) {
  let result = await Filesystem.readdir({
    directory,
    path: dirPath,
  })
  console.log('result:', result)
  fileList.innerHTML = /* html */ `
  <button>up</button>
  <p>
    dir: ${dirPath}
  </p>
  <p>
    ${result.files.length} files:
  </p>
  <div style="margin: 1rem 0">
    <input placeholder="search by filename"/>
  </div>
  `
  fileList.querySelector('button')!.onclick = function () {
    let parts = dirPath.split('/')
    if (parts.length > 1) {
      parts.pop()
    }
    showDir(directory, parts.join('/'))
  }
  let input = fileList.querySelector('input')!
  input.oninput = function () {
    let text = input.value.toLowerCase()
    for (let file of result.files) {
      let fileNode = (file as any).node as HTMLElement
      fileNode.hidden = !file.name.toLowerCase().includes(text)
    }
  }
  result.files.sort((a, b) => b.mtime - a.mtime)
  for (let file of result.files) {
    let ext = file.name.split('.').pop()!
    if (
      file.type === 'file' &&
      (!file.name.includes('.') || skipExts.includes(ext))
    ) {
      continue
    }
    let fileNode = fileTemplate.cloneNode(true) as HTMLElement
    fileNode.querySelector('.file-type')!.textContent = file.type
    fileNode.querySelector('.file-name')!.textContent = file.name
    fileNode.querySelector('.file-size')!.textContent = format_byte(file.size)
    fileNode.querySelector('.file-mtime')!.textContent = format_long_short_time(
      file.mtime,
    )
    fileNode.onclick = async function () {
      let filePath = dirPath + '/' + file.name
      if (file.type == 'directory') {
        showDir(directory, filePath)
      } else if (file.type == 'file') {
        audioNode.pause()
        videoNode.pause()
        audioNode.hidden = true
        videoNode.hidden = true
        let mime = (videoExts.includes(ext) ? 'video' : 'audio') + '/' + ext
        console.log('mime:', mime)
        let result = await Filesystem.readFile({
          directory,
          path: filePath,
        })
        if (videoExts.includes(ext)) {
          let mime = 'video/' + ext
          videoNode.hidden = false
          videoNode.src = `data:${mime};base64,${result.data}`
          videoNode.play()
        } else {
          let mime = 'audio/' + ext
          audioNode.hidden = false
          audioNode.src = `data:${mime};base64,${result.data}`
          audioNode.play()
        }
      }
    }
    Object.assign(file, { node: fileNode })
    fileList.appendChild(fileNode)
  }
}

let skipExts = ['txt']

let videoExts = ['mp4']
let audiosExts = ['mp3', 'm4a']

let fileTemplate = fileList.children[0]
fileTemplate.remove()
