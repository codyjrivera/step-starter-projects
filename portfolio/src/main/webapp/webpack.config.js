const autoprefixer = require('autoprefixer');
const glob = require('glob');
const path = require('path');

module.exports = [{
  entry: {
    app : [path.resolve(__dirname, "src/app.js"), path.resolve(__dirname, "src/app.scss")],
    bio : [path.resolve(__dirname, "src/bio.js"), path.resolve(__dirname, "src/bio.scss")],
    exp : [path.resolve(__dirname, "src/exp.js"), path.resolve(__dirname, "src/exp.scss")],
    proj : [path.resolve(__dirname, "src/proj.js"), path.resolve(__dirname, "src/proj.scss")],
    act : [path.resolve(__dirname, "src/act.js"), path.resolve(__dirname, "src/act.scss")],
    com : [path.resolve(__dirname, "src/com.js"), path.resolve(__dirname, "src/com.scss")],
  },
  output: {
    path: path.resolve(__dirname, "src/dist"),
    filename: '[name].js',
  },
  module: {
    rules: [
      {
        test: /\.scss$/,
        use: [
          {
            loader: 'file-loader',
            options: {
              name: '[name].css',
            },
          },
          { loader: 'extract-loader' },
          { loader: 'css-loader' },
          {
            loader: 'postcss-loader',
            options: {
               plugins: () => [autoprefixer()]
            }
          },
          {
            loader: 'sass-loader',
            options: {
              includePaths: ['./node_modules']
            }
          }
        ]
      },
      {
        test: /\.js$/,
        loader: 'babel-loader',
        query: {
          presets: ['@babel/preset-env']
        },
      }
    ]
  },
}];
