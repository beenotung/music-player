import { Capacitor } from '@capacitor/core'
import { Directory, Encoding, Filesystem } from '@capacitor/filesystem'
import { format_byte, format_long_short_time } from '@beenotung/tslib/format'

declare var statusNode: HTMLElement
declare var audioNode: HTMLAudioElement
declare var videoNode: HTMLAudioElement
declare var playingNameNode: HTMLElement
declare var dirList: HTMLElement
declare var fileList: HTMLElement
declare var messageNode: HTMLElement

let storage: {
  viewDir: Directory
  viewPath: string
  palyDir: Directory
  palyPath: string
} = localStorage as any

statusNode.textContent = 'status: loaded script'

audioNode.hidden = true
videoNode.hidden = true

let dirTemplate = dirList.children[0]
dirTemplate.remove()
for (let key in Directory) {
  let directory = (Directory as any)[key] as Directory
  let dirNode = dirTemplate.cloneNode(true) as HTMLElement
  let button = dirNode.querySelector('button')!
  if (directory == storage.viewDir) {
    dirNode.classList.add('selected')
  }
  button.textContent = key
  button.onclick = async function () {
    let status = await Filesystem.requestPermissions()
    messageNode.textContent = 'Permission: ' + status.publicStorage
    dirList
      .querySelectorAll('.dir.selected')
      .forEach(e => e.classList.remove('selected'))
    dirNode.classList.add('selected')
    showDir(directory, '.')
  }
  dirList.appendChild(dirNode)
}

async function showDir(directory: Directory, dirPath: string) {
  storage.viewDir = directory
  storage.viewPath = dirPath
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
      if (fileNode) {
        fileNode.hidden = !file.name.toLowerCase().includes(text)
      }
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

    let filePath = dirPath + '/' + file.name
    if (filePath == storage.palyPath) {
      fileNode.classList.add('playing')
    }

    async function openDir() {
      showDir(directory, filePath)
    }

    async function addDir() {}

    async function addFile() {}

    let playButton = fileNode.querySelector('.play-button') as HTMLButtonElement
    let addButton = fileNode.querySelector('.add-button') as HTMLButtonElement

    if (file.type === 'directory') {
      fileNode.onclick = openDir
    } else if (file.type === 'file') {
      fileNode.onclick = () => {
        fileList
          .querySelectorAll('.file.playing')
          .forEach(e => e.classList.remove('playing'))
        fileNode.classList.add('playing')
        playFile(directory, filePath, 'play')
      }
    }
    playButton.remove()
    addButton.remove()

    Object.assign(file, { node: fileNode })
    fileList.appendChild(fileNode)
  }
}

async function playFile(
  directory: Directory,
  filePath: string,
  mode: 'play' | 'select',
) {
  storage.palyDir = directory
  storage.palyPath = filePath
  audioNode.pause()
  videoNode.pause()
  audioNode.hidden = true
  videoNode.hidden = true
  let ext = filePath.split('.').pop()!
  let type = videoExts.includes(ext) ? 'video' : 'audio'
  let mime = type + '/' + ext
  console.log('mime:', mime)
  let result = await Filesystem.readFile({
    directory,
    path: filePath,
  })
  if (type == 'video') {
    videoNode.hidden = false
    videoNode.src = `data:${mime};base64,${result.data}`
    if (mode == 'play') {
      videoNode.play()
    }
  } else {
    audioNode.hidden = false
    audioNode.src = `data:${mime};base64,${result.data}`
    if (mode == 'play') {
      audioNode.play()
    }
  }
  playingNameNode.textContent = filePath.split('/').pop()!
  messageNode.textContent = ''
}

audioNode.onerror = function () {
  messageNode.textContent = audioNode.error!.message
  fileList.querySelector('.file.playing')?.classList.add('error')
}
videoNode.onerror = function () {
  messageNode.textContent = videoNode.error!.message
  fileList.querySelector('.file.playing')?.classList.add('error')
}

let skipExts = ['txt', 'dashAudio']

let videoExts = ['mp4']
let audiosExts = ['mp3', 'm4a']

let fileTemplate = fileList.children[0]
fileTemplate.remove()

function restore() {
  {
    let directory = storage.viewDir
    let dirPath = storage.viewPath
    if (directory && dirPath) {
      showDir(directory as Directory, dirPath)
    }
  }
  {
    let directory = storage.palyDir
    let filePath = storage.palyPath
    if (directory && filePath) {
      playFile(directory, filePath, 'select')
    }
  }
}
restore()
