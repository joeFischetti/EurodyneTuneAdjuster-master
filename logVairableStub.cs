//Some commands that get sent immediately before sending the logging addresses
List<byte> list4 = new List<byte>
{
        44,
        2,
        242,
        0,
        20
};

//Build the list of logging addresses, and put them in the prior array
foreach (LogVariable logVariable2 in list2)
{
        byte[] collection = this.ReverseTheOrder(BitConverter.GetBytes(logVariable2.Address));
        list4.AddRange(collection);
        list4.Add(this.GetVariableDataLength(logVariable2));
}

//Send 16,79
this.result = can15765Helper.SendCanMessage15765(med9SerialPortController, this.canID, new byte[]
{
        16,
        79
}, new CAN15765MessageParser(), 1000);

//Security
this.PassSecuritySiemens(med9SerialPortController, this.canID, can15765Helper, 3);

//Send 44, 3, 242, 0
this.result = can15765Helper.SendCanMessage15765(med9SerialPortController, this.canID, new byte[]
{
        44,
        3,
        242,
        0
}, new CAN15765MessageParser(), 1000);

//Send the array we built before
this.result = can15765Helper.SendCanMessage15765(med9SerialPortController, this.canID, list4.ToArray(), new CAN15765MessageParser(), 1000);

//loop, and read the data that came back
while (flexibleLoggingForm.logging)
{
        do
        {
                CAN15765Helper can15765Helper2 = can15765Helper;
                MED9SerialPortController serialPortController = med9SerialPortController;
                CanIdChannels canIdChannels = this.canID;
                byte[] array = new byte[3];
                array[0] = 34;
                array[1] = 242;
                this.result = can15765Helper2.SendCanMessage15765(serialPortController, canIdChannels, array, new CAN15765MessageParser(), 1500);
        }
        while (this.result.ResultCode != ActionResultCode.Success);
        list3 = this.result.Response;
        list3.RemoveRange(0, 3);
        flexibleLoggingForm.UpdateLogHS(list3, list2);
}
