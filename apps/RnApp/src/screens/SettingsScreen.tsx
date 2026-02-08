import React, {useEffect, useState} from 'react';
import {
  DeviceEventEmitter,
  NativeModules,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';

const {AppBridge} = NativeModules;

interface SettingsProps {
  userName?: string;
  appVersion?: string;
  themeName?: string;
}

interface ThemeEvent {
  theme: string;
  timestamp: number;
}

function SettingsScreen(props: SettingsProps) {
  const [lastEvent, setLastEvent] = useState<ThemeEvent | null>(null);
  const [appInfo, setAppInfo] = useState<Record<string, string> | null>(null);

  // Native → RN: DeviceEventEmitter 이벤트 수신
  useEffect(() => {
    const subscription = DeviceEventEmitter.addListener(
      'onThemeChanged',
      (event: ThemeEvent) => {
        setLastEvent(event);
      },
    );
    return () => subscription.remove();
  }, []);

  // RN → Native: Toast 표시
  const handleShowToast = () => {
    AppBridge.showToast('RN에서 보낸 메시지입니다!');
  };

  // RN → Native: 네이티브 화면 전환
  const handleNavigateHome = () => {
    AppBridge.navigateToNativeScreen('home');
  };

  // RN → Native: Promise 기반 앱 정보 조회
  const handleGetAppInfo = async () => {
    const info = await AppBridge.getAppInfo();
    setAppInfo(info);
  };

  // RN → Native → RN: 테마 변경 요청 (양방향 통신)
  const handleThemeChange = () => {
    const nextTheme = lastEvent?.theme === 'dark' ? 'light' : 'dark';
    AppBridge.requestThemeChange(nextTheme);
  };

  return (
    <ScrollView
      style={styles.scrollView}
      contentContainerStyle={styles.container}>
      <Text style={styles.title}>설정</Text>
      <Text style={styles.subtitle}>React Native에서 렌더링됨</Text>

      {/* Native → RN: initialProperties로 전달받은 데이터 */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>초기 데이터 (initialProperties)</Text>
        <View style={styles.card}>
          <InfoRow label="사용자" value={props.userName ?? '(없음)'} />
          <InfoRow label="앱 버전" value={props.appVersion ?? '(없음)'} />
          <InfoRow label="테마" value={props.themeName ?? '(없음)'} />
        </View>
      </View>

      {/* RN → Native: NativeModule 메서드 호출 */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>RN → Native 통신</Text>
        <Button label="Toast 표시" onPress={handleShowToast} />
        <Button label="홈 화면으로 이동" onPress={handleNavigateHome} />
        <Button label="앱 정보 조회 (Promise)" onPress={handleGetAppInfo} />
        {appInfo && (
          <View style={styles.card}>
            {Object.entries(appInfo).map(([key, value]) => (
              <InfoRow key={key} label={key} value={String(value)} />
            ))}
          </View>
        )}
      </View>

      {/* Native → RN: DeviceEventEmitter 이벤트 */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Native → RN 이벤트</Text>
        <Button label="테마 변경 요청" onPress={handleThemeChange} />
        {lastEvent ? (
          <View style={styles.card}>
            <InfoRow label="수신 테마" value={lastEvent.theme} />
            <InfoRow
              label="타임스탬프"
              value={new Date(lastEvent.timestamp).toLocaleTimeString()}
            />
          </View>
        ) : (
          <Text style={styles.hint}>
            버튼을 누르면 Native에서 이벤트가 전송됩니다
          </Text>
        )}
      </View>

      {/* SharedPreferences 공유 저장소 */}
      <SharedStorageSection />
    </ScrollView>
  );
}

function InfoRow({label, value}: {label: string; value: string}) {
  return (
    <View style={styles.infoRow}>
      <Text style={styles.infoLabel}>{label}</Text>
      <Text style={styles.infoValue}>{value}</Text>
    </View>
  );
}

const STORAGE_KEY = 'shared_text';

function SharedStorageSection() {
  const [inputText, setInputText] = useState('');
  const [loadedText, setLoadedText] = useState<string | null>(null);

  const handleSave = () => {
    AppBridge.saveText(STORAGE_KEY, inputText);
    AppBridge.showToast('저장 완료');
  };

  const handleLoad = async () => {
    const value = await AppBridge.loadText(STORAGE_KEY);
    setLoadedText(value);
  };

  return (
    <View style={styles.section}>
      <Text style={styles.sectionTitle}>SharedPreferences 공유 저장소</Text>
      <TextInput
        style={styles.textInput}
        value={inputText}
        onChangeText={setInputText}
        placeholder="텍스트 입력"
        placeholderTextColor="#aaa"
      />
      <Button label="저장" onPress={handleSave} />
      <Button label="불러오기" onPress={handleLoad} />
      <View style={styles.card}>
        <Text style={styles.loadedText}>
          {loadedText ?? '(저장된 값 없음)'}
        </Text>
      </View>
    </View>
  );
}

function Button({label, onPress}: {label: string; onPress: () => void}) {
  return (
    <Pressable
      style={({pressed}) => [styles.button, pressed && styles.buttonPressed]}
      onPress={onPress}>
      <Text style={styles.buttonText}>{label}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  scrollView: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  container: {
    padding: 24,
    paddingTop: 48,
    paddingBottom: 40,
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 4,
  },
  subtitle: {
    fontSize: 16,
    color: '#6750A4',
    marginBottom: 24,
  },
  section: {
    marginBottom: 24,
  },
  sectionTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#999',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginBottom: 8,
  },
  card: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: {width: 0, height: 1},
    shadowOpacity: 0.2,
    shadowRadius: 2,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 6,
  },
  infoLabel: {
    fontSize: 14,
    color: '#666',
  },
  infoValue: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
  },
  button: {
    backgroundColor: '#6750A4',
    borderRadius: 12,
    paddingVertical: 14,
    paddingHorizontal: 20,
    alignItems: 'center',
    marginBottom: 8,
  },
  buttonPressed: {
    opacity: 0.8,
  },
  buttonText: {
    color: '#fff',
    fontSize: 15,
    fontWeight: '600',
  },
  hint: {
    fontSize: 13,
    color: '#aaa',
    textAlign: 'center',
    marginTop: 8,
  },
  textInput: {
    backgroundColor: '#fff',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#ddd',
    paddingVertical: 12,
    paddingHorizontal: 16,
    fontSize: 15,
    color: '#333',
    marginBottom: 8,
  },
  loadedText: {
    fontSize: 15,
    color: '#333',
    textAlign: 'center',
  },
});

export default SettingsScreen;
