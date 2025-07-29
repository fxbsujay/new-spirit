export function createSvgIconsPlugin(opt) {

    const cache = new Map()

    let isBuild = false
    const options = {
      svgoOptions: true,
      symbolId: 'icon-[dir]-[name]',
      inject: 'body-last',
      customDomId: '__svg__icons__dom__',
      ...opt,
    }

    let { svgoOptions } = options
    const { symbolId } = options

    if (!symbolId.includes('[name]')) {
      throw new Error('SymbolId must contain [name] string!')
    }

    if (svgoOptions) {
      svgoOptions = typeof svgoOptions === 'boolean' ? {} : svgoOptions
    }

    
}