-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 30, 2025 at 08:46 AM
-- Server version: 10.4.20-MariaDB
-- PHP Version: 8.0.8

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `db_absensi`
--

-- --------------------------------------------------------

--
-- Table structure for table `tabsensi`
--

CREATE TABLE `tabsensi` (
  `idabsen` int(11) NOT NULL,
  `idkaryawan` varchar(10) DEFAULT NULL,
  `tanggal` date DEFAULT NULL,
  `jammasuk` time DEFAULT NULL,
  `jamistirahat` time DEFAULT NULL,
  `jamkembali` time DEFAULT NULL,
  `jampulang` time DEFAULT NULL,
  `id_keterangan` int(11) DEFAULT NULL,
  `idshift` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `tabsensi`
--

INSERT INTO `tabsensi` (`idabsen`, `idkaryawan`, `tanggal`, `jammasuk`, `jamistirahat`, `jamkembali`, `jampulang`, `id_keterangan`, `idshift`) VALUES
(1, 'K001', '2025-05-29', '08:00:00', '12:00:00', '13:00:00', '17:00:00', 1, 1),
(2, 'K002', '2025-05-29', '08:15:00', '12:00:00', '13:15:00', '17:00:00', 3, 2),
(3, 'K003', '2025-05-29', '08:00:00', '12:00:00', '13:00:00', '18:30:00', 4, 3),
(4, 'K001', '2025-05-30', '07:31:01', '07:36:49', NULL, NULL, 1, 1),
(5, 'K002', '2025-05-30', '07:31:04', '07:31:08', '07:31:12', '07:31:15', 1, 1),
(6, 'K005', '2025-05-30', '07:34:55', '10:09:21', '10:09:24', '10:09:27', 0, 1),
(7, 'K004', '2025-05-30', '07:37:42', '10:09:08', '10:09:11', '10:09:13', 0, 1);

-- --------------------------------------------------------

--
-- Table structure for table `tjabatan`
--

CREATE TABLE `tjabatan` (
  `idjabatan` int(11) NOT NULL,
  `jabatan` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `tjabatan`
--

INSERT INTO `tjabatan` (`idjabatan`, `jabatan`) VALUES
(1, 'Staff'),
(2, 'Manager'),
(3, 'Supervisor');

-- --------------------------------------------------------

--
-- Table structure for table `tkaryawan`
--

CREATE TABLE `tkaryawan` (
  `idkaryawan` varchar(10) NOT NULL,
  `namakaryawan` varchar(50) DEFAULT NULL,
  `idjabatan` int(11) DEFAULT NULL,
  `idprodi` int(11) DEFAULT NULL,
  `idshift` int(11) DEFAULT NULL,
  `hadir` int(11) DEFAULT NULL,
  `terlambat` int(11) DEFAULT NULL,
  `terlambat_kembali` int(11) DEFAULT NULL,
  `lembur` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `tkaryawan`
--

INSERT INTO `tkaryawan` (`idkaryawan`, `namakaryawan`, `idjabatan`, `idprodi`, `idshift`, `hadir`, `terlambat`, `terlambat_kembali`, `lembur`) VALUES
('K001', 'Andi Wijaya', 1, 1, 1, 5, 0, 0, 2),
('K002', 'Siti Aminah', 2, 3, 2, 3, 1, 1, 0),
('K003', 'Budi Santoso', 3, 2, 3, 2, 0, 0, 1),
('K004', 'Dzikri', 3, 3, NULL, NULL, NULL, NULL, NULL),
('K005', 'Mr Tarmin', 1, 2, NULL, NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `tketerangan`
--

CREATE TABLE `tketerangan` (
  `id_keterangan` int(11) NOT NULL,
  `keterangan` enum('Hadir','Terlambat','Terlambat Kembali','Lembur') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `tketerangan`
--

INSERT INTO `tketerangan` (`id_keterangan`, `keterangan`) VALUES
(0, ''),
(1, 'Hadir'),
(2, 'Terlambat'),
(3, 'Terlambat Kembali'),
(4, 'Lembur');

-- --------------------------------------------------------

--
-- Table structure for table `tprodi`
--

CREATE TABLE `tprodi` (
  `idprodi` int(11) NOT NULL,
  `prodi` enum('teknik','faster','hukum','febi','fkip') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `tprodi`
--

INSERT INTO `tprodi` (`idprodi`, `prodi`) VALUES
(1, 'teknik'),
(2, 'faster'),
(3, 'hukum'),
(4, 'febi'),
(5, 'fkip');

-- --------------------------------------------------------

--
-- Table structure for table `trole`
--

CREATE TABLE `trole` (
  `roleid` int(11) NOT NULL,
  `datacreate` datetime DEFAULT NULL,
  `datamodify` date DEFAULT NULL,
  `namarole` varchar(45) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `trole`
--

INSERT INTO `trole` (`roleid`, `datacreate`, `datamodify`, `namarole`) VALUES
(1, '2025-05-29 15:49:13', '2025-05-29', 'admin'),
(2, '2025-05-29 15:49:13', '2025-05-29', 'user');

-- --------------------------------------------------------

--
-- Table structure for table `tshift`
--

CREATE TABLE `tshift` (
  `idshift` int(11) NOT NULL,
  `namashift` enum('Pagi','Siang','Sore') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `tshift`
--

INSERT INTO `tshift` (`idshift`, `namashift`) VALUES
(1, 'Pagi'),
(2, 'Siang'),
(3, 'Sore');

-- --------------------------------------------------------

--
-- Table structure for table `tuser`
--

CREATE TABLE `tuser` (
  `iduser` varchar(8) NOT NULL,
  `namauser` varchar(45) DEFAULT NULL,
  `password` int(11) DEFAULT NULL,
  `roleid` int(11) DEFAULT NULL,
  `datacreate` datetime DEFAULT NULL,
  `datamodify` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `tuser`
--

INSERT INTO `tuser` (`iduser`, `namauser`, `password`, `roleid`, `datacreate`, `datamodify`) VALUES
('12345678', 'dzikri1234', 123456, 1, '2025-05-29 20:57:09', '2025-05-29 20:57:09');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `tabsensi`
--
ALTER TABLE `tabsensi`
  ADD PRIMARY KEY (`idabsen`),
  ADD KEY `idkaryawan` (`idkaryawan`),
  ADD KEY `id_keterangan` (`id_keterangan`),
  ADD KEY `idshift` (`idshift`);

--
-- Indexes for table `tjabatan`
--
ALTER TABLE `tjabatan`
  ADD PRIMARY KEY (`idjabatan`);

--
-- Indexes for table `tkaryawan`
--
ALTER TABLE `tkaryawan`
  ADD PRIMARY KEY (`idkaryawan`),
  ADD KEY `idjabatan` (`idjabatan`),
  ADD KEY `idprodi` (`idprodi`),
  ADD KEY `idshift` (`idshift`);

--
-- Indexes for table `tketerangan`
--
ALTER TABLE `tketerangan`
  ADD PRIMARY KEY (`id_keterangan`);

--
-- Indexes for table `tprodi`
--
ALTER TABLE `tprodi`
  ADD PRIMARY KEY (`idprodi`);

--
-- Indexes for table `trole`
--
ALTER TABLE `trole`
  ADD PRIMARY KEY (`roleid`);

--
-- Indexes for table `tshift`
--
ALTER TABLE `tshift`
  ADD PRIMARY KEY (`idshift`);

--
-- Indexes for table `tuser`
--
ALTER TABLE `tuser`
  ADD PRIMARY KEY (`iduser`),
  ADD KEY `roleid` (`roleid`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `tabsensi`
--
ALTER TABLE `tabsensi`
  MODIFY `idabsen` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `tabsensi`
--
ALTER TABLE `tabsensi`
  ADD CONSTRAINT `tabsensi_ibfk_1` FOREIGN KEY (`idkaryawan`) REFERENCES `tkaryawan` (`idkaryawan`),
  ADD CONSTRAINT `tabsensi_ibfk_2` FOREIGN KEY (`id_keterangan`) REFERENCES `tketerangan` (`id_keterangan`),
  ADD CONSTRAINT `tabsensi_ibfk_3` FOREIGN KEY (`idshift`) REFERENCES `tshift` (`idshift`);

--
-- Constraints for table `tkaryawan`
--
ALTER TABLE `tkaryawan`
  ADD CONSTRAINT `tkaryawan_ibfk_1` FOREIGN KEY (`idjabatan`) REFERENCES `tjabatan` (`idjabatan`),
  ADD CONSTRAINT `tkaryawan_ibfk_2` FOREIGN KEY (`idprodi`) REFERENCES `tprodi` (`idprodi`),
  ADD CONSTRAINT `tkaryawan_ibfk_3` FOREIGN KEY (`idshift`) REFERENCES `tshift` (`idshift`);

--
-- Constraints for table `tuser`
--
ALTER TABLE `tuser`
  ADD CONSTRAINT `tuser_ibfk_1` FOREIGN KEY (`roleid`) REFERENCES `trole` (`roleid`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
