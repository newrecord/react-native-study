/**
 * @format
 */

import {AppRegistry} from 'react-native';
import App from './App';
import SettingsScreen from './src/screens/SettingsScreen';
import {name as appName} from './app.json';

// 기본 RnApp 진입점 (독립 실행 시)
AppRegistry.registerComponent(appName, () => App);

// 브라운필드 진입점: AndroidApp의 설정 탭에서 이 모듈을 로드
AppRegistry.registerComponent('SettingsModule', () => SettingsScreen);
