import {nextTick} from "vue";


/**
 * 生成棋盘
 * @param {svg} svg
 * @param {number} width  宽几列
 * @param {number} height  高几行
 * @param {number} squareSize  棋盘大小
 * @param {boolean} showLabel 是否显示行号
 * @returns {{ss: number, ox: number, oy: number}}
 */
export const computeBoard = (svg, width, height, squareSize, showLabel) => {

    if (!squareSize || squareSize <= 0) {
        squareSize = 14
    }

    const ss = showLabel ? squareSize : 0
    let ox = ss + Math.round(squareSize / 2)
    let oy = ss + Math.round(squareSize / 2)

    const TINY_SQUARE_SIZE = 10

    const lineWidth = squareSize > TINY_SQUARE_SIZE ? Math.round(2 * Math.round(Math.max(1, squareSize * 0.02))) * 0.5 : squareSize * 0.08
    ox -= lineWidth * 0.5
    oy -= lineWidth * 0.5

    ox = Math.round(ox * 2.0) * 0.5
    oy = Math.round(oy * 2.0) * 0.5

    let pathStr = ''

    for (let x = 0; x < width; ++x) {
        pathStr += `M ${ ox + x * squareSize } ${ oy } L ${ ox + x * squareSize } ${
            oy + (height - 1) * squareSize
        } `
    }
    for (let y = 0; y < height; ++y) {
        pathStr += `M ${ ox } ${ oy + y * squareSize } L ${ ox + (width - 1) * squareSize } ${
            oy + y * squareSize
        } `
    }

    let points = null
    if (width === 19 && height === 19) {
        points = [
            [3, 3],
            [3, 9],
            [3, 15],
            [9, 3],
            [9, 9],
            [9, 15],
            [15, 3],
            [15, 9],
            [15, 15]
        ]
    } else if (width === 13 && height === 13) {
        points = [
            [3, 3],
            [3, 9],
            [6, 6],
            [9, 3],
            [9, 9],
        ]
    } else if (width === 9 && height === 9) {
        points = [
            [2, 2],
            [2, 6],
            [4, 4],
            [6, 2],
            [6, 6]
        ]
    }

    const linesLayer = document.createElementNS("http://www.w3.org/2000/svg", "g")

    if (points) {
        const r = squareSize < 5 ? 0.5 : Math.max(2, squareSize * 0.075);
        for (let i = 0; i < points.length; ++i) {
            const [hx, hy] = points[i];
            const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle')
            circle.setAttribute('cx', (ox + hx * squareSize).toString())
            circle.setAttribute('cy', (oy + hy * squareSize).toString())
            circle.setAttribute('r', `${r.toFixed(1)}px`)
            circle.setAttribute('fill', '#000000')
            linesLayer.appendChild(circle)
        }
    }

    const linesPath = document.createElementNS("http://www.w3.org/2000/svg", "path")

    linesPath.setAttribute('d', pathStr)
    linesPath.setAttribute('stroke', '#000000')

    if (squareSize > TINY_SQUARE_SIZE) {
        linesPath.setAttribute('stroke-width', `${lineWidth.toFixed(0)}px`)
    } else {
        linesPath.setAttribute('stroke-width', `${lineWidth.toFixed(1)}px`)
    }

    linesPath.setAttribute('stroke-linecap', 'square')

    linesLayer.appendChild(linesPath)


    let labelsLayer = null
    if (showLabel) {
        labelsLayer = document.createElementNS("http://www.w3.org/2000/svg", "g")
        labelsLayer.setAttribute('class', 'coordinate-labels')
        let textSize = Math.round(squareSize * 0.4)

        let textOffset = textSize / 2.5

        function createText(str, x, y) {
            const text = document.createElementNS("http://www.w3.org/2000/svg", "text")
            text.setAttribute("x", x)
            text.setAttribute("y", y)
            text.setAttribute("font-size", `${Math.round(textSize)}px`)
            text.setAttribute("font-size", `${Math.round(textSize)}px`)
            text.setAttribute("font-weight", '')
            text.setAttribute("fill", '#444444')
            text.textContent = str
            return text
        }

        for (let i = 1; i <= width; ++i) {
            const x = i * squareSize + squareSize / 2 - textOffset
            const y = (squareSize / 2) + (squareSize / 4)
            labelsLayer.appendChild(createText(PRETTY_COORDINATE_SEQUENCE[i - 1], x.toFixed(0), y.toFixed(0)))
            const lastY = (height + 1) * squareSize + (squareSize / 2)
            labelsLayer.appendChild(createText(PRETTY_COORDINATE_SEQUENCE[i - 1], x.toFixed(0), lastY.toFixed(0)))
        }

        for (let i = 1; i <= height; ++i) {
            let x = (squareSize / 2.5)
            const y = i * squareSize + squareSize / 2 + textOffset
            const text = i.toString()
            if (text.length >= 2) {
                x = x - (text.length * (textSize * 0.2))
            }
            labelsLayer.appendChild(createText(text, x.toFixed(0), y.toFixed(0)))
            const lastX = (width + 1) * squareSize + x
            labelsLayer.appendChild(createText(text, lastX.toFixed(0), y.toFixed(0)))
        }
    }

    nextTick(() => {
        svg.value.setAttribute('width', squareSize * (width + (showLabel ? 2 : 0)))
        svg.value.setAttribute('height', squareSize * (height + (showLabel ? 2 : 0)))
        if (showLabel) {
            svg.value.prepend(labelsLayer)
        }
        svg.value.prepend(linesLayer)
    })

    let mid = squareSize / 2
    if (squareSize % 2 === 0) {
        mid -= 0.5
    }
    const radius = Math.max(1, squareSize * 0.5)
    return {
        ss,
        ox,
        oy,
        mid,
        squareSize,
        w: width,
        h: height,
        showLabel,
        point: {
            shadowSize: mid * 1.2,
            shadowRadius: radius * 0.95,
            offset: mid - radius
        }
    }
}

export const PRETTY_COORDINATE_SEQUENCE = 'ABCDEFGHJKLMNOPQRSTUVWXYZ'