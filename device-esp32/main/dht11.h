#ifndef DHT_11
#define DHT_11

/**
 * @struct dht11_t
 * @brief DHT11 传感器的读取数据结构，包含传感器的引脚信息和上次读取的温湿度数据
 *
 * @var dht11_pin 连接 DHT11 传感器的引脚
 * @var temperature 上次读取的温度值
 * @var humidity 上次读取的湿度值
 */
typedef struct {
    int dht11_pin;     /**< DHT11 引脚 */
    float temperature; /**< 温度值，单位：摄氏度 */
    float humidity;    /**< 湿度值，单位：百分比 */
} dht11_t;

/**
 * @brief 等待指定引脚进入目标状态
 *
 * @param dht11 DHT11 传感器实例
 * @param state 目标状态，0 表示低电平，1 表示高电平
 * @param timeout 等待超时的最大时间，单位：毫秒
 * @return 成功时返回等待的时间，超时返回 -1
 */
int wait_for_state(dht11_t dht11, int state, int timeout);

/**
 * @brief 使指定引脚保持低电平状态指定时间
 *
 * @param dht11 DHT11 传感器实例
 * @param hold_time_us 保持低电平的时间，单位：微秒
 */
void hold_low(dht11_t dht11, int hold_time_us);

/**
 * @brief 从 DHT11 传感器读取温度和湿度值
 *
 * @note 该函数为阻塞式，即 CPU 会在读取传感器期间一直等待，直到读取完成
 * @note 最好每次读取之间间隔至少 2 秒钟，避免数据错误
 *
 * @param dht11 指向 dht11_t 结构体的指针，存储读取到的温湿度数据
 * @param connection_timeout 连接超时的最大尝试次数
 * @return 成功时返回 0，失败时返回错误代码
 */
int dht11_read(dht11_t *dht11, int connection_timeout);

#endif
