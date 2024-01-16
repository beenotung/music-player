import { Capacitor } from '@capacitor/core'
import { Directory, Encoding, Filesystem } from '@capacitor/filesystem'

declare var statusNode: HTMLElement
declare var platformNode: HTMLElement
declare var dirList: HTMLInputElement
declare var fileList: HTMLInputElement
declare var messageNode: HTMLElement

statusNode.textContent = 'status: loaded script'
platformNode.textContent = Capacitor.getPlatform()

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

async function showDir(directory: Directory, path: string) {
  let result = await Filesystem.readdir({
    directory,
    path,
  })
  console.log('result:', result)
  fileList.innerHTML = /* html */ `
  <button>up</button>
  <p>
    path: ${path}
  </p>
  <p>
    ${result.files.length} files:
  </p>
  <div style="margin: 1rem 0">
    <input placeholder="search by filename"/>
  </div>
  `
  fileList.querySelector('button')!.onclick = function () {
    let parts = path.split('/')
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
    let fileNode = fileTemplate.cloneNode(true) as HTMLElement
    fileNode.querySelector('.file-type')!.textContent = file.type
    fileNode.querySelector('.file-name')!.textContent = file.name
    fileNode.querySelector('.file-mtime')!.textContent = new Date(
      file.mtime,
    ).toLocaleString()
    fileNode.querySelector('.file-uri')!.textContent = file.uri
    fileNode.onclick = function () {
      showDir(directory, path + '/' + file.name)
    }
    Object.assign(file, { node: fileNode })
    fileList.appendChild(fileNode)
  }
}

let fileTemplate = fileList.children[0]
fileTemplate.remove()
